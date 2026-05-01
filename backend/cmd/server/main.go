package main

import (
	"log"
	"net/http"

	"github.com/acc1111/aircraft-war-hitsz/backend/internal/app"
)

func main() {
	// 小项目直接使用固定数据库文件，避免再引入额外环境变量配置。
	server, err := app.NewServer(app.Config{DBPath: "backend/aircraft-war.sqlite"})
	if err != nil {
		log.Fatal(err)
	}
	defer server.Close()
	log.Fatal(http.ListenAndServe(":8080", server.Handler()))
}
