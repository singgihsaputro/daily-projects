use axum::{body::Body, http::{Request, StatusCode}};
use http_body_util::BodyExt;
use serde_json::{Value, json};
use sleep_log::{app, model, store::FixtureRepo};
use std::sync::Arc;
use tower::ServiceExt;

fn seeded() -> axum::Router {
    app(Arc::new(FixtureRepo::from_file("mock/entries.json").unwrap()))
}

async fn call(router: &axum::Router, method: &str, uri: &str, body: Option<Value>) -> (StatusCode, Value) {
    let req = Request::builder().method(method).uri(uri).header("content-type", "application/json")
        .body(body.map_or(Body::empty(), |b| Body::from(b.to_string()))).unwrap();
    let res = router.clone().oneshot(req).await.unwrap();
    let status = res.status();
    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    (status, serde_json::from_slice(&bytes).unwrap_or(Value::Null))
}

#[test]
fn duration_crosses_midnight() {
    assert_eq!(model::minutes_asleep("23:10", "06:40"), Some(450));
    assert_eq!(model::minutes_asleep("01:00", "09:00"), Some(480));
    assert_eq!(model::minutes_asleep("25:00", "09:00"), None);
}

#[test]
fn dates_are_validated() {
    assert!(model::parse_date("2026-02-29").is_none());
    assert_eq!(model::parse_date("1970-01-02"), Some(1));
    assert!(model::parse_date("2028-02-29").is_some());
}

#[tokio::test]
async fn lists_newest_first_with_durations() {
    let (s, v) = call(&seeded(), "GET", "/entries", None).await;
    assert_eq!(s, StatusCode::OK);
    assert_eq!(v[0]["date"], "2026-10-12");
    assert_eq!(v[0]["duration"], "7h 20m");
}

#[tokio::test]
async fn create_validates_and_rejects_duplicates() {
    let r = seeded();
    let ok = json!({"date":"2026-10-13","bedtime":"23:00","wake":"07:00","quality":4});
    let (s, v) = call(&r, "POST", "/entries", Some(ok.clone())).await;
    assert_eq!((s, v["id"].as_u64()), (StatusCode::CREATED, Some(8)));
    assert_eq!(call(&r, "POST", "/entries", Some(ok)).await.0, StatusCode::CONFLICT);
    let bad = json!({"date":"2026-10-14","bedtime":"23:00","wake":"07:00","quality":9});
    assert_eq!(call(&r, "POST", "/entries", Some(bad)).await.0, StatusCode::UNPROCESSABLE_ENTITY);
    let junk = json!({"date":"nope"});
    assert_eq!(call(&r, "POST", "/entries", Some(junk)).await.0, StatusCode::UNPROCESSABLE_ENTITY);
}

#[tokio::test]
async fn show_and_delete() {
    let r = seeded();
    assert_eq!(call(&r, "GET", "/entries/3", None).await.0, StatusCode::OK);
    assert_eq!(call(&r, "DELETE", "/entries/3", None).await.0, StatusCode::NO_CONTENT);
    assert_eq!(call(&r, "GET", "/entries/3", None).await.0, StatusCode::NOT_FOUND);
    assert_eq!(call(&r, "DELETE", "/entries/3", None).await.0, StatusCode::NOT_FOUND);
}

#[tokio::test]
async fn summary_window_and_bounds() {
    let r = seeded();
    let (s, v) = call(&r, "GET", "/summary?days=3", None).await;
    assert_eq!(s, StatusCode::OK);
    assert_eq!(v["nights"], 3);
    assert_eq!(v["from"], "2026-10-10");
    let (_, week) = call(&r, "GET", "/summary", None).await;
    assert_eq!(week["nights"], 7);
    assert_eq!(call(&r, "GET", "/summary?days=0", None).await.0, StatusCode::UNPROCESSABLE_ENTITY);
}
