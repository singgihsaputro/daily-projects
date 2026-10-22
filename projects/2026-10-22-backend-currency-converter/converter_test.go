package main

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func fixture(t *testing.T) http.Handler {
	t.Helper()
	return newMux(FileSource{Path: "mock/rates.json"})
}

func get(h http.Handler, url string) *httptest.ResponseRecorder {
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest("GET", url, nil))
	return rec
}

func TestConvertRoundsToMinorUnits(t *testing.T) {
	tbl, err := FileSource{Path: "mock/rates.json"}.Load()
	if err != nil {
		t.Fatal(err)
	}
	_, got, err := tbl.Convert("USD", "JPY", 100)
	if err != nil || got != 15142 {
		t.Fatalf("USD->JPY 100 = %v, %v; want 15142", got, err)
	}
	_, got, _ = tbl.Convert("EUR", "EUR", 12.345)
	if got != 12.35 && got != 12.34 {
		t.Fatalf("identity rounding gave %v", got)
	}
}

func TestCrossRateIsInverseConsistent(t *testing.T) {
	tbl, _ := FileSource{Path: "mock/rates.json"}.Load()
	a, _ := tbl.Rate("GBP", "IDR")
	b, _ := tbl.Rate("IDR", "GBP")
	if d := a*b - 1; d > 1e-9 || d < -1e-9 {
		t.Fatalf("rate*inverse = %v", a*b)
	}
}

func TestConvertEndpoint(t *testing.T) {
	rec := get(fixture(t), "/api/convert?from=usd&to=eur&amount=10")
	if rec.Code != 200 || !strings.Contains(rec.Body.String(), `"result":9.21`) {
		t.Fatalf("got %d %s", rec.Code, rec.Body)
	}
}

func TestBadRequests(t *testing.T) {
	h := fixture(t)
	cases := map[string]int{
		"/api/convert?from=USD&to=EUR&amount=abc": 400,
		"/api/convert?from=USD&to=EUR&amount=-1":  400,
		"/api/convert?from=USD&to=XXX&amount=1":   404,
		"/api/rates?base=ZZZ":                     404,
	}
	for url, want := range cases {
		if got := get(h, url).Code; got != want {
			t.Errorf("%s = %d, want %d", url, got, want)
		}
	}
}

func TestBatch(t *testing.T) {
	h := fixture(t)
	post := func(body string) int {
		rec := httptest.NewRecorder()
		h.ServeHTTP(rec, httptest.NewRequest("POST", "/api/convert/batch", strings.NewReader(body)))
		return rec.Code
	}
	if c := post(`{"from":"USD","amount":5,"targets":["EUR","JPY"]}`); c != 200 {
		t.Errorf("valid batch = %d", c)
	}
	if c := post(`{"from":"USD","amount":5,"targets":[]}`); c != 400 {
		t.Errorf("empty targets = %d", c)
	}
	if c := post(`{"from":"USD","amount":5,"targets":["NOPE"]}`); c != 422 {
		t.Errorf("unknown target = %d", c)
	}
	if c := post(`not json`); c != 400 {
		t.Errorf("bad json = %d", c)
	}
}

func TestSourceFailureIs502(t *testing.T) {
	h := newMux(FileSource{Path: "mock/missing.json"})
	if c := get(h, "/api/currencies").Code; c != 502 {
		t.Fatalf("got %d, want 502", c)
	}
}
