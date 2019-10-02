pushd ~/Projects/vlingo/vlingo-lattice-exchange-rabbitmq
docker-compose down
docker volume rm $(docker volume ls -q)
docker-compose up -d
popd
