package main

import (
	"encoding/json"
	"net/http"
	"strings"
)

type Server struct {
	store Store
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(v)
}

func writeError(w http.ResponseWriter, status int, msg string) {
	writeJSON(w, status, map[string]string{"error": msg})
}

// groupIDAndRest splits "/api/groups/{id}/rest" into ("{id}", "rest").
func groupIDAndRest(path string) (string, string) {
	trimmed := strings.TrimPrefix(path, "/api/groups/")
	parts := strings.SplitN(trimmed, "/", 2)
	if len(parts) == 1 {
		return parts[0], ""
	}
	return parts[0], parts[1]
}

func (s *Server) handleGroups(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "only GET is supported")
		return
	}
	writeJSON(w, http.StatusOK, s.store.ListGroups())
}

func (s *Server) handleGroupSubroutes(w http.ResponseWriter, r *http.Request) {
	id, rest := groupIDAndRest(r.URL.Path)
	if id == "" {
		writeError(w, http.StatusNotFound, "group id required")
		return
	}
	group, ok := s.store.GetGroup(id)
	if !ok {
		writeError(w, http.StatusNotFound, "group not found")
		return
	}

	switch rest {
	case "":
		writeJSON(w, http.StatusOK, group)
	case "expenses":
		s.handleExpenses(w, r, group)
	case "balances":
		s.handleBalances(w, r, group)
	default:
		writeError(w, http.StatusNotFound, "unknown route")
	}
}

func (s *Server) handleExpenses(w http.ResponseWriter, r *http.Request, group Group) {
	switch r.Method {
	case http.MethodGet:
		writeJSON(w, http.StatusOK, s.store.ListExpenses(group.ID))
	case http.MethodPost:
		var in Expense
		if err := json.NewDecoder(r.Body).Decode(&in); err != nil {
			writeError(w, http.StatusBadRequest, "invalid JSON body")
			return
		}
		created, err := s.store.AddExpense(group.ID, in)
		if err != nil {
			writeError(w, http.StatusUnprocessableEntity, err.Error())
			return
		}
		writeJSON(w, http.StatusCreated, created)
	default:
		writeError(w, http.StatusMethodNotAllowed, "only GET and POST are supported")
	}
}

func (s *Server) handleBalances(w http.ResponseWriter, r *http.Request, group Group) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "only GET is supported")
		return
	}
	expenses := s.store.ListExpenses(group.ID)
	net := Net(group.Members, expenses)
	writeJSON(w, http.StatusOK, map[string]any{
		"net":         net,
		"settlements": Settle(net),
	})
}

func NewMux(store Store) *http.ServeMux {
	s := &Server{store: store}
	mux := http.NewServeMux()
	mux.HandleFunc("/api/groups", s.handleGroups)
	mux.HandleFunc("/api/groups/", s.handleGroupSubroutes)
	return mux
}
