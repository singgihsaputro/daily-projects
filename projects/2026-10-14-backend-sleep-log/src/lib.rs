pub mod model;
pub mod store;

use axum::{
    Json, Router,
    extract::{Path, Query, State, rejection::JsonRejection},
    http::StatusCode,
    routing::get,
};
use model::{EntryView, NewEntry, Summary};
use serde::Deserialize;
use serde_json::{Value, json};
use std::sync::Arc;
use store::EntryRepo;

type Repo = Arc<dyn EntryRepo>;
type ApiError = (StatusCode, Json<Value>);

fn err(code: StatusCode, msg: impl Into<String>) -> ApiError {
    (code, Json(json!({ "error": msg.into() })))
}

pub fn app(repo: Repo) -> Router {
    Router::new()
        .route("/health", get(|| async { Json(json!({ "status": "ok" })) }))
        .route("/entries", get(list).post(create))
        .route("/entries/{id}", get(show).delete(remove))
        .route("/summary", get(summary))
        .with_state(repo)
}

async fn list(State(repo): State<Repo>) -> Json<Vec<EntryView>> {
    Json(repo.list().into_iter().map(model::view).collect())
}

async fn show(State(repo): State<Repo>, Path(id): Path<u32>) -> Result<Json<EntryView>, ApiError> {
    repo.list().into_iter().find(|e| e.id == id).map(|e| Json(model::view(e))).ok_or_else(|| err(StatusCode::NOT_FOUND, "no such entry"))
}

async fn create(
    State(repo): State<Repo>,
    body: Result<Json<NewEntry>, JsonRejection>,
) -> Result<(StatusCode, Json<EntryView>), ApiError> {
    let Json(new) = body.map_err(|e| err(StatusCode::UNPROCESSABLE_ENTITY, e.body_text()))?;
    model::validate(&new).map_err(|m| err(StatusCode::UNPROCESSABLE_ENTITY, m))?;
    if repo.list().iter().any(|e| e.date == new.date) {
        return Err(err(StatusCode::CONFLICT, "an entry for that date already exists"));
    }
    Ok((StatusCode::CREATED, Json(model::view(repo.add(new)))))
}

async fn remove(State(repo): State<Repo>, Path(id): Path<u32>) -> Result<StatusCode, ApiError> {
    if repo.remove(id) { Ok(StatusCode::NO_CONTENT) } else { Err(err(StatusCode::NOT_FOUND, "no such entry")) }
}

#[derive(Deserialize)]
struct SummaryQuery {
    days: Option<i64>,
}

async fn summary(State(repo): State<Repo>, Query(q): Query<SummaryQuery>) -> Result<Json<Summary>, ApiError> {
    let days = q.days.unwrap_or(7);
    if !(1..=365).contains(&days) {
        return Err(err(StatusCode::UNPROCESSABLE_ENTITY, "days must be between 1 and 365"));
    }
    Ok(Json(model::summarize(&repo.list(), days)))
}
