.PHONY: clean build build-thin install deploy stop projects deps verify help

help: ## 显示帮助信息
	@echo ""
	@echo "可用命令:"
	@echo "  make clean      - 清理构建产物"
	@echo "  make build-thin - 编译打包（跳过测试、检查、文档）"
	@echo "  make build      - 编译打包（全量，含测试）"
	@echo "  make install    - 安装到本地 Maven 仓库（跳过测试、检查、文档）"
	@echo "  make deploy     - 发布到 Nexus 私服（跳过测试、检查、文档）"
	@echo "  make verify     - 验证依赖安全基线（版本声明 + Jackson 运行时）"
	@echo "  make stop       - 停止所有 Gradle Daemon"
	@echo "  make projects   - 查看所有子项目"
	@echo "  make deps       - 查看依赖树"
	@echo ""

# 清理构建产物
clean:
	./gradlew clean

# 全量构建（含测试）
build: clean
	./gradlew build

# 精简构建（跳过测试、Checkstyle、Asciidoctor、Javadoc）
build-thin: clean
	./gradlew build -x test -x checkstyleMain -x checkstyleTest -x javadoc

# 安装到本地 Maven 仓库（跳过测试、检查、文档）
install:
	./gradlew clean publishToMavenLocal -x test -x checkstyleMain -x checkstyleTest -x javadoc

# 发布到 Nexus 私服（跳过测试、检查、文档）
deploy:
	./gradlew clean publish -x test -x checkstyleMain -x checkstyleTest -x javadoc

# 验证依赖安全基线（buildSrc 版本检查 + Jackson 运行时 smoke test）
verify:
	./gradlew test -p buildSrc
	./gradlew verifyDependencySecurity :springfox-schema:test --tests "springfox.documentation.schema.JacksonRuntimeVersionSpec"

# 停止所有 Gradle Daemon
stop:
	./gradlew --stop

# 查看所有子项目
projects:
	./gradlew projects

# 查看依赖树
deps:
	./gradlew dependencies --configuration compile
