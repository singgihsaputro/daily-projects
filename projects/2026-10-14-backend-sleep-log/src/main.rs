use sleep_log::{app, store::FixtureRepo};
use std::sync::Arc;

#[tokio::main]
async fn main() {
    let path = std::env::var("SLEEP_FIXTURE").unwrap_or_else(|_| "mock/entries.json".into());
    let repo = FixtureRepo::from_file(&path).unwrap_or_else(|e| {
        eprintln!("cannot load fixture: {e}");
        std::process::exit(1)
    });
    let addr = std::env::var("ADDR").unwrap_or_else(|_| "127.0.0.1:3000".into());
    let listener = tokio::net::TcpListener::bind(&addr).await.expect("bind");
    println!("sleep-log listening on http://{addr}");
    axum::serve(listener, app(Arc::new(repo))).await.expect("serve");
}
