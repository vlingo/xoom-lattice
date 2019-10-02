# bounce the docker volume vlingo-lattice-exchange-rabbitmq in the docker-compose.yml
docker-compose -p "dev" down
docker volume rm vlingo-lattice-exchange-rabbitmq
docker-compose -p "dev" up -d
