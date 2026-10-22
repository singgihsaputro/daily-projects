package main

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"
)

const maxBatch = 50

type server struct{ src RateSource }

func newMux(src RateSource) http.Handler {
	s := &server{src: src}
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
	})
	mux.HandleFunc("GET /api/currencies", s.currencies)
	mux.HandleFunc("GET /api/rates", s.rates)
	mux.HandleFunc("GET /api/convert", s.convert)
	mux.HandleFunc("POST /api/convert/batch", s.batch)
	return mux
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(v)
}

func writeErr(w http.ResponseWriter, status int, msg string) {
	writeJSON(w, status, map[string]string{"error": msg})
}

func (s *server) table(w http.ResponseWriter) (RateTable, bool) {
	t, err := s.src.Load()
	if err != nil {
		writeErr(w, http.StatusBadGateway, "rate source unavailable: "+err.Error())
		return RateTable{}, false
	}
	return t, true
}

func (s *server) currencies(w http.ResponseWriter, r *http.Request) {
	if t, ok := s.table(w); ok {
		writeJSON(w, http.StatusOK, t.Currencies)
	}
}

func (s *server) rates(w http.ResponseWriter, r *http.Request) {
	t, ok := s.table(w)
	if !ok {
		return
	}
	base := r.URL.Query().Get("base")
	if base == "" {
		base = t.Base
	}
	rates, err := t.RatesFor(base)
	if err != nil {
		writeErr(w, http.StatusNotFound, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"base": strings.ToUpper(base), "asOf": t.AsOf, "rates": rates,
	})
}

type conversion struct {
	From   string  `json:"from"`
	To     string  `json:"to"`
	Amount float64 `json:"amount"`
	Rate   float64 `json:"rate"`
	Result float64 `json:"result"`
	AsOf   string  `json:"asOf"`
}

func (s *server) convert(w http.ResponseWriter, r *http.Request) {
	t, ok := s.table(w)
	if !ok {
		return
	}
	q := r.URL.Query()
	amount, err := strconv.ParseFloat(q.Get("amount"), 64)
	if err != nil || amount < 0 {
		writeErr(w, http.StatusBadRequest, "amount must be a non-negative number")
		return
	}
	rate, result, err := t.Convert(q.Get("from"), q.Get("to"), amount)
	if err != nil {
		writeErr(w, http.StatusNotFound, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, conversion{
		From: strings.ToUpper(q.Get("from")), To: strings.ToUpper(q.Get("to")),
		Amount: amount, Rate: rate, Result: result, AsOf: t.AsOf,
	})
}

type batchRequest struct {
	From    string   `json:"from"`
	Amount  float64  `json:"amount"`
	Targets []string `json:"targets"`
}

func (s *server) batch(w http.ResponseWriter, r *http.Request) {
	var req batchRequest
	dec := json.NewDecoder(http.MaxBytesReader(w, r.Body, 1<<16))
	dec.DisallowUnknownFields()
	if err := dec.Decode(&req); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid JSON body: "+err.Error())
		return
	}
	if req.Amount < 0 || len(req.Targets) == 0 || len(req.Targets) > maxBatch {
		writeErr(w, http.StatusBadRequest, "need amount >= 0 and 1-50 targets")
		return
	}
	t, ok := s.table(w)
	if !ok {
		return
	}
	out := make([]conversion, 0, len(req.Targets))
	for _, to := range req.Targets {
		rate, result, err := t.Convert(req.From, to, req.Amount)
		if err != nil {
			writeErr(w, http.StatusUnprocessableEntity, err.Error())
			return
		}
		out = append(out, conversion{
			From: strings.ToUpper(req.From), To: strings.ToUpper(to),
			Amount: req.Amount, Rate: rate, Result: result, AsOf: t.AsOf,
		})
	}
	writeJSON(w, http.StatusOK, map[string]any{"results": out})
}
