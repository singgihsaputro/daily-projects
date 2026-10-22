package main

import (
	"encoding/json"
	"fmt"
	"math"
	"os"
	"strings"
)

// Currency is one entry of the rate table. PerBase is how many units of this
// currency one unit of the table's base currency buys.
type Currency struct {
	Code     string  `json:"code"`
	Name     string  `json:"name"`
	Decimals int     `json:"decimals"`
	PerBase  float64 `json:"perBase"`
}

// RateTable is a snapshot of exchange rates.
type RateTable struct {
	Base       string     `json:"base"`
	AsOf       string     `json:"asOf"`
	Currencies []Currency `json:"currencies"`
}

// RateSource supplies the current rate table. Swap FileSource for an HTTP
// client implementing this interface to go live; nothing else changes.
type RateSource interface {
	Load() (RateTable, error)
}

// FileSource reads rates from a JSON fixture on disk.
type FileSource struct{ Path string }

func (f FileSource) Load() (RateTable, error) {
	raw, err := os.ReadFile(f.Path)
	if err != nil {
		return RateTable{}, err
	}
	var t RateTable
	if err := json.Unmarshal(raw, &t); err != nil {
		return RateTable{}, fmt.Errorf("parse %s: %w", f.Path, err)
	}
	for _, c := range t.Currencies {
		if c.PerBase <= 0 {
			return RateTable{}, fmt.Errorf("currency %s has non-positive rate", c.Code)
		}
	}
	return t, nil
}

func (t RateTable) find(code string) (Currency, bool) {
	code = strings.ToUpper(strings.TrimSpace(code))
	for _, c := range t.Currencies {
		if c.Code == code {
			return c, true
		}
	}
	return Currency{}, false
}

// Rate returns the cross rate: units of `to` per one unit of `from`.
func (t RateTable) Rate(from, to string) (float64, error) {
	f, ok := t.find(from)
	if !ok {
		return 0, fmt.Errorf("unknown currency %q", from)
	}
	g, ok := t.find(to)
	if !ok {
		return 0, fmt.Errorf("unknown currency %q", to)
	}
	return g.PerBase / f.PerBase, nil
}

// Convert converts amount and rounds to the target currency's minor units.
func (t RateTable) Convert(from, to string, amount float64) (rate, result float64, err error) {
	rate, err = t.Rate(from, to)
	if err != nil {
		return 0, 0, err
	}
	g, _ := t.find(to)
	scale := math.Pow10(g.Decimals)
	return rate, math.Round(amount*rate*scale) / scale, nil
}

// RatesFor returns every currency quoted against `base`, keyed by code.
func (t RateTable) RatesFor(base string) (map[string]float64, error) {
	out := make(map[string]float64, len(t.Currencies))
	for _, c := range t.Currencies {
		r, err := t.Rate(base, c.Code)
		if err != nil {
			return nil, err
		}
		out[c.Code] = r
	}
	return out, nil
}
