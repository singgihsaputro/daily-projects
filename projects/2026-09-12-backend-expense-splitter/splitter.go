package main

import "sort"

// Net returns each member's balance: positive means the group owes them,
// negative means they owe the group. Every expense is split evenly across
// its SplitBetween list.
func Net(members []string, expenses []Expense) map[string]float64 {
	net := make(map[string]float64, len(members))
	for _, m := range members {
		net[m] = 0
	}
	for _, e := range expenses {
		share := e.Amount / float64(len(e.SplitBetween))
		net[e.PaidBy] += e.Amount
		for _, m := range e.SplitBetween {
			net[m] -= share
		}
	}
	return net
}

type Settlement struct {
	From   string  `json:"from"`
	To     string  `json:"to"`
	Amount float64 `json:"amount"`
}

// Settle turns net balances into a minimal set of payments that zero
// everyone out, by repeatedly matching the largest debtor with the largest
// creditor.
func Settle(net map[string]float64) []Settlement {
	type entry struct {
		name    string
		balance float64
	}
	var entries []entry
	for name, balance := range net {
		entries = append(entries, entry{name, round2(balance)})
	}
	sort.Slice(entries, func(i, j int) bool { return entries[i].name < entries[j].name })

	var settlements []Settlement
	for {
		lowIdx, highIdx := -1, -1
		for i, e := range entries {
			if e.balance < -0.005 && (lowIdx == -1 || e.balance < entries[lowIdx].balance) {
				lowIdx = i
			}
			if e.balance > 0.005 && (highIdx == -1 || e.balance > entries[highIdx].balance) {
				highIdx = i
			}
		}
		if lowIdx == -1 || highIdx == -1 {
			break
		}
		amount := min(-entries[lowIdx].balance, entries[highIdx].balance)
		amount = round2(amount)
		settlements = append(settlements, Settlement{
			From:   entries[lowIdx].name,
			To:     entries[highIdx].name,
			Amount: amount,
		})
		entries[lowIdx].balance += amount
		entries[highIdx].balance -= amount
	}
	return settlements
}

func round2(v float64) float64 {
	return float64(int(v*100+0.5*sign(v))) / 100
}

func sign(v float64) float64 {
	if v < 0 {
		return -1
	}
	return 1
}
