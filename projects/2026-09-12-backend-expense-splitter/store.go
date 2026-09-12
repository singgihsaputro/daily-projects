package main

import (
	"encoding/json"
	"fmt"
	"os"
	"sync"
)

type Group struct {
	ID      string   `json:"id"`
	Name    string   `json:"name"`
	Members []string `json:"members"`
}

type Expense struct {
	ID           string   `json:"id"`
	GroupID      string   `json:"groupId"`
	Description  string   `json:"description"`
	Amount       float64  `json:"amount"`
	PaidBy       string   `json:"paidBy"`
	SplitBetween []string `json:"splitBetween"`
}

// Store is the seam between the HTTP layer and the data source. JSONStore
// backs it with local fixtures; a real deployment could swap in a database
// implementation without touching handlers.go.
type Store interface {
	ListGroups() []Group
	GetGroup(id string) (Group, bool)
	ListExpenses(groupID string) []Expense
	AddExpense(groupID string, e Expense) (Expense, error)
}

type JSONStore struct {
	mu       sync.Mutex
	groups   []Group
	expenses []Expense
	nextID   int
}

func NewJSONStore(groupsPath, expensesPath string) (*JSONStore, error) {
	var groups []Group
	if err := loadFixture(groupsPath, &groups); err != nil {
		return nil, err
	}
	var expenses []Expense
	if err := loadFixture(expensesPath, &expenses); err != nil {
		return nil, err
	}
	return &JSONStore{groups: groups, expenses: expenses, nextID: len(expenses) + 1}, nil
}

func loadFixture(path string, into any) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return fmt.Errorf("read fixture %s: %w", path, err)
	}
	if err := json.Unmarshal(data, into); err != nil {
		return fmt.Errorf("parse fixture %s: %w", path, err)
	}
	return nil
}

func (s *JSONStore) ListGroups() []Group {
	s.mu.Lock()
	defer s.mu.Unlock()
	out := make([]Group, len(s.groups))
	copy(out, s.groups)
	return out
}

func (s *JSONStore) GetGroup(id string) (Group, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	for _, g := range s.groups {
		if g.ID == id {
			return g, true
		}
	}
	return Group{}, false
}

func (s *JSONStore) ListExpenses(groupID string) []Expense {
	s.mu.Lock()
	defer s.mu.Unlock()
	var out []Expense
	for _, e := range s.expenses {
		if e.GroupID == groupID {
			out = append(out, e)
		}
	}
	return out
}

func (s *JSONStore) AddExpense(groupID string, e Expense) (Expense, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	var group *Group
	for i := range s.groups {
		if s.groups[i].ID == groupID {
			group = &s.groups[i]
			break
		}
	}
	if group == nil {
		return Expense{}, fmt.Errorf("group %q not found", groupID)
	}
	if !memberOf(group.Members, e.PaidBy) {
		return Expense{}, fmt.Errorf("paidBy %q is not a member of group %q", e.PaidBy, groupID)
	}
	for _, m := range e.SplitBetween {
		if !memberOf(group.Members, m) {
			return Expense{}, fmt.Errorf("splitBetween member %q is not in group %q", m, groupID)
		}
	}
	if e.Amount <= 0 {
		return Expense{}, fmt.Errorf("amount must be positive")
	}
	if len(e.SplitBetween) == 0 {
		return Expense{}, fmt.Errorf("splitBetween must not be empty")
	}

	e.ID = fmt.Sprintf("e%d", s.nextID)
	e.GroupID = groupID
	s.nextID++
	s.expenses = append(s.expenses, e)
	return e, nil
}

func memberOf(members []string, name string) bool {
	for _, m := range members {
		if m == name {
			return true
		}
	}
	return false
}
