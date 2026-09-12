package main

import (
	"log"
	"net/http"
	"os"
)

func main() {
	store, err := NewJSONStore("mock/groups.json", "mock/expenses.json")
	if err != nil {
		log.Fatalf("failed to load fixtures: %v", err)
	}

	addr := ":8080"
	if p := os.Getenv("PORT"); p != "" {
		addr = ":" + p
	}

	log.Printf("expense-splitter listening on %s", addr)
	if err := http.ListenAndServe(addr, NewMux(store)); err != nil {
		log.Fatal(err)
	}
}
