## Запуск проекта

- `Запуск исполняемого Jar`

    - выполняем clean bootJar
    - переходим в директорию приложения/build/libs
    - выполняем ./intershop-0.0.1-SNAPSHOT.jar
- `Запуск через докер контейнер`
  - выполняем clean bootJar
  - переходим в директорию приложения
  - выполняем docker-compose up -d       
  - в созданом postres container'е создаем схему intershop
  - перезапускаем контейнер приложения
    - либо в [application.yml](src/main/resources/application.yml) переопределяем значение schemaName и default-schema на public