use serde::{Deserialize, Serialize};

pub const GOAL_MINUTES: i64 = 8 * 60;

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Entry {
    pub id: u32,
    /// Calendar date of the morning you woke up, `YYYY-MM-DD`.
    pub date: String,
    pub bedtime: String,
    pub wake: String,
    pub quality: u8,
    #[serde(default)]
    pub note: String,
}

#[derive(Deserialize)]
pub struct NewEntry {
    pub date: String,
    pub bedtime: String,
    pub wake: String,
    pub quality: u8,
    #[serde(default)]
    pub note: String,
}

#[derive(Serialize)]
pub struct EntryView {
    #[serde(flatten)]
    pub entry: Entry,
    pub minutes: i64,
    pub duration: String,
}

#[derive(Serialize, Debug, PartialEq)]
pub struct Summary {
    pub nights: usize,
    pub from: Option<String>,
    pub to: Option<String>,
    pub avg_minutes: i64,
    pub avg_duration: String,
    pub avg_quality: f64,
    /// Minutes short of the 8h goal, summed over the window (never negative per night).
    pub sleep_debt_minutes: i64,
    /// Standard deviation of bedtime in minutes; lower is more regular.
    pub bedtime_spread_minutes: i64,
}

/// Parses `HH:MM` into minutes since midnight.
pub fn parse_clock(s: &str) -> Option<i64> {
    let (h, m) = s.split_once(':')?;
    if h.len() != 2 || m.len() != 2 {
        return None;
    }
    let (h, m): (i64, i64) = (h.parse().ok()?, m.parse().ok()?);
    (h < 24 && m < 60).then_some(h * 60 + m)
}

/// Days since 1970-01-01 for a valid `YYYY-MM-DD`, else `None`.
pub fn parse_date(s: &str) -> Option<i64> {
    let mut it = s.split('-');
    let (y, m, d): (i64, i64, i64) = (it.next()?.parse().ok()?, it.next()?.parse().ok()?, it.next()?.parse().ok()?);
    if it.next().is_some() || s.len() != 10 || !(1..=12).contains(&m) {
        return None;
    }
    let leap = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0);
    let dim = [31, if leap { 29 } else { 28 }, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][m as usize - 1];
    if !(1..=dim).contains(&d) {
        return None;
    }
    let y = if m <= 2 { y - 1 } else { y };
    let era = y.div_euclid(400);
    let yoe = y - era * 400;
    let doy = (153 * ((m + 9) % 12) + 2) / 5 + d - 1;
    let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    Some(era * 146097 + doe - 719468)
}

/// Minutes asleep, treating a wake time at or before bedtime as the next day.
pub fn minutes_asleep(bedtime: &str, wake: &str) -> Option<i64> {
    let (b, w) = (parse_clock(bedtime)?, parse_clock(wake)?);
    Some(if w > b { w - b } else { w + 24 * 60 - b })
}

pub fn fmt_duration(minutes: i64) -> String {
    format!("{}h {:02}m", minutes / 60, minutes % 60)
}

pub fn validate(n: &NewEntry) -> Result<(), String> {
    if parse_date(&n.date).is_none() {
        return Err("date must be a real calendar date, YYYY-MM-DD".into());
    }
    if parse_clock(&n.bedtime).is_none() || parse_clock(&n.wake).is_none() {
        return Err("bedtime and wake must be HH:MM (24h)".into());
    }
    if parse_clock(&n.bedtime) == parse_clock(&n.wake) {
        return Err("bedtime and wake must differ".into());
    }
    if !(1..=5).contains(&n.quality) {
        return Err("quality must be between 1 and 5".into());
    }
    if n.note.len() > 200 {
        return Err("note must be at most 200 characters".into());
    }
    Ok(())
}

pub fn view(entry: Entry) -> EntryView {
    let minutes = minutes_asleep(&entry.bedtime, &entry.wake).unwrap_or(0);
    EntryView { duration: fmt_duration(minutes), minutes, entry }
}

/// Summarises the last `days` calendar days ending at the newest entry.
pub fn summarize(entries: &[Entry], days: i64) -> Summary {
    let dated: Vec<(i64, &Entry)> = entries.iter().filter_map(|e| Some((parse_date(&e.date)?, e))).collect();
    let newest = dated.iter().map(|(d, _)| *d).max();
    let window: Vec<&Entry> = match newest {
        Some(n) => dated.iter().filter(|(d, _)| *d > n - days).map(|(_, e)| *e).collect(),
        None => vec![],
    };
    if window.is_empty() {
        return Summary { nights: 0, from: None, to: None, avg_minutes: 0, avg_duration: fmt_duration(0), avg_quality: 0.0, sleep_debt_minutes: 0, bedtime_spread_minutes: 0 };
    }
    let n = window.len() as i64;
    let mins: Vec<i64> = window.iter().map(|e| minutes_asleep(&e.bedtime, &e.wake).unwrap_or(0)).collect();
    let avg = mins.iter().sum::<i64>() / n;
    // Bedtimes after midnight count as "late evening" so 23:30 and 00:30 are one hour apart.
    let beds: Vec<f64> = window
        .iter()
        .map(|e| {
            let b = parse_clock(&e.bedtime).unwrap_or(0);
            (if b < 12 * 60 { b + 24 * 60 } else { b }) as f64
        })
        .collect();
    let mean = beds.iter().sum::<f64>() / beds.len() as f64;
    let var = beds.iter().map(|b| (b - mean).powi(2)).sum::<f64>() / beds.len() as f64;
    let quality = window.iter().map(|e| e.quality as f64).sum::<f64>() / n as f64;
    let mut dates: Vec<&str> = window.iter().map(|e| e.date.as_str()).collect();
    dates.sort();
    Summary {
        nights: window.len(),
        from: dates.first().map(|s| s.to_string()),
        to: dates.last().map(|s| s.to_string()),
        avg_minutes: avg,
        avg_duration: fmt_duration(avg),
        avg_quality: (quality * 10.0).round() / 10.0,
        sleep_debt_minutes: mins.iter().map(|m| (GOAL_MINUTES - m).max(0)).sum(),
        bedtime_spread_minutes: var.sqrt().round() as i64,
    }
}
