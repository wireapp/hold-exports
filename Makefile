run:
	./gradlew run

docker-build:
	docker build -t lukaswire/hold-exports:latest .

publish: docker-build
	docker push lukaswire/hold-exports:latest