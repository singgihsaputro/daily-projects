use crate::model::{Entry, NewEntry};
use std::sync::Mutex;

/// Everything the API needs from persistence. Swap the implementation to move
/// from the JSON fixture to a real database without touching the handlers.
pub trait EntryRepo: Send + Sync {
    fn list(&self) -> Vec<Entry>;
    fn add(&self, new: NewEntry) -> Entry;
    fn remove(&self, id: u32) -> bool;
}

pub struct FixtureRepo {
    entries: Mutex<Vec<Entry>>,
}

impl FixtureRepo {
    pub fn from_file(path: &str) -> Result<Self, String> {
        let raw = std::fs::read_to_string(path).map_err(|e| format!("{path}: {e}"))?;
        let entries = serde_json::from_str(&raw).map_err(|e| format!("{path}: {e}"))?;
        Ok(Self { entries: Mutex::new(entries) })
    }

    pub fn empty() -> Self {
        Self { entries: Mutex::new(vec![]) }
    }
}

impl EntryRepo for FixtureRepo {
    fn list(&self) -> Vec<Entry> {
        let mut all = self.entries.lock().unwrap().clone();
        all.sort_by(|a, b| b.date.cmp(&a.date));
        all
    }

    fn add(&self, n: NewEntry) -> Entry {
        let mut all = self.entries.lock().unwrap();
        let id = all.iter().map(|e| e.id).max().unwrap_or(0) + 1;
        let entry = Entry { id, date: n.date, bedtime: n.bedtime, wake: n.wake, quality: n.quality, note: n.note };
        all.push(entry.clone());
        entry
    }

    fn remove(&self, id: u32) -> bool {
        let mut all = self.entries.lock().unwrap();
        let before = all.len();
        all.retain(|e| e.id != id);
        all.len() != before
    }
}
