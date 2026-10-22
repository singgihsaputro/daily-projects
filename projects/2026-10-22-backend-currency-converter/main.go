package main

import (
	"log"
	"net/http"
	"os"
)

func main() {
	path := os.Getenv("RATES_FILE")
	if path == "" {
		path = "mock/rates.json"
	}
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	src := FileSource{Path: path}
	if _, err := src.Load(); err != nil {
		log.Fatalf("cannot load rates: %v", err)
	}
	log.Printf("currency converter listening on :%s (rates: %s)", port, path)
	log.Fatal(http.ListenAndServe(":"+port, newMux(src)))
}
