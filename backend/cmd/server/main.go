package main

import (
	"log"
	"net/http"
	"os"

	"github.com/acc1111/aircraft-war-hitsz/backend/internal/app"
)

func main() {
	dbPath := os.Getenv("AIRCRAFT_WAR_DB_PATH")
	if dbPath == "" {
		dbPath = "backend.sqlite"
	}
	server, err := app.NewServer(app.Config{DBPath: dbPath})
	if err != nil {
		log.Fatal(err)
	}
	defer server.Close()
	log.Fatal(http.ListenAndServe(":8080", server.Handler()))
}
