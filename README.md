# Housing Utilities System Admin

Система управления жилищно-коммунальными услугами на Spring Boot 3.5.5 с MongoDB Replica Set.

## 🚀 Быстрый старт (Windows 10)

### Требования
- [Docker Desktop](https://www.docker.com/products/docker-desktop) для Windows
- Минимум 8 ГБ RAM
- 10 ГБ свободного места на диске

### Запуск

#### Способ 1: Использование скрипта (рекомендуется)

1. **Откройте PowerShell** в директории проекта

2. **Запустите приложение**:
   ```powershell
   .\start-app.ps1
   ```

3. **Откройте браузер**: http://localhost:8080

#### Способ 2: Запуск без скрипта через Docker Compose

1. **Откройте PowerShell** в директории проекта

2. **Запустите все сервисы**:
   ```powershell
   docker-compose up -d
   ```

3. **Дождитесь запуска** (первый запуск может занять 5-10 минут)

4. **Проверьте статус**:
   ```powershell
   docker-compose ps
   ```

5. **Откройте браузер**: http://localhost:8080

**Готово!** Приложение запустится с MongoDB Replica Set (3 реплики).

⏱️ Первый запуск занимает **5-10 минут** (загрузка образов + сборка)

---

## 📝 Управление приложением

### Основные команды (через скрипты)

```powershell
# Запуск приложения
.\start-app.ps1

# Остановка приложения
.\stop-app.ps1

# Проверка статуса
.\status-app.ps1

# Просмотр логов
.\logs-app.ps1

# Полная очистка (с удалением данных)
.\cleanup-app.ps1
```

### Команды Docker Compose (без скриптов)

```powershell
# Запуск всех сервисов
docker-compose up -d

# Остановка всех сервисов
docker-compose down

# Остановка с удалением данных
docker-compose down -v

# Просмотр логов приложения
docker-compose logs -f app

# Просмотр логов всех сервисов
docker-compose logs -f

# Проверка статуса сервисов
docker-compose ps

# Перезапуск приложения
docker-compose restart app

# Пересборка и перезапуск
docker-compose up --build -d

# Остановка конкретного сервиса
docker-compose stop app

# Запуск конкретного сервиса
docker-compose start app
```

---

## 🗄️ Архитектура

### Сервисы
- **Spring Boot приложение** - порт 8080
- **MongoDB Replica Set**:
  - mongo1 (PRIMARY) - порт 27017
  - mongo2 (SECONDARY) - порт 27018
  - mongo3 (SECONDARY) - порт 27019

### Автоматическая загрузка данных
Тестовые данные загружаются при первом запуске:
- 4897 домов
- 100 улиц
- 10 городов
- 200 пользователей
- 10 председателей
- Счета, контакты, голосования

---

## 🔧 Технологии

- **Backend**: Java 17, Spring Boot 3.5.5
- **База данных**: MongoDB latest (Replica Set)
- **Безопасность**: Spring Security, OAuth2, JWT
- **Шаблонизатор**: Thymeleaf
- **Маппинг**: MapStruct
- **Сборка**: Maven
- **Контейнеризация**: Docker, Docker Compose

---

## 🔄 Профили Spring Boot

Приложение поддерживает несколько профилей конфигурации:

### Доступные профили

1. **dev** (по умолчанию) - Локальная разработка
   - Подключение к локальной MongoDB
   - Подробное логирование (DEBUG)
   - Файл: `application-dev.yml`

2. **prod** - Продакшен
   - Подключение к MongoDB Replica Set в prod
   - Минимальное логирование (INFO/WARN)
   - Поддержка переменных окружения для секретов
   - Файл: `application-prod.yml`

3. **docker** - Docker окружение
   - Подключение к MongoDB через имена сервисов Docker
   - Файл: `application-docker.yml`

### Переключение профилей

#### В Docker (docker-compose.yml)
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

#### При запуске JAR файла
```powershell
# Профиль dev
java -jar app.jar --spring.profiles.active=dev

# Профиль prod
java -jar app.jar --spring.profiles.active=prod

# Профиль docker
java -jar app.jar --spring.profiles.active=docker
```

#### В IDE (IntelliJ IDEA / Eclipse)
1. Run -> Edit Configurations
2. В поле "VM options" или "Program arguments" добавьте:
   ```
   --spring.profiles.active=dev
   ```

#### Через переменную окружения
```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="dev"
java -jar app.jar

# Windows CMD
set SPRING_PROFILES_ACTIVE=dev
java -jar app.jar
```

---

## 🐛 Решение проблем

### Приложение не запускается
1. Убедитесь, что **Docker Desktop запущен**
2. Проверьте логи: `.\logs-app.ps1` или `docker-compose logs -f`
3. Проверьте статус: `.\status-app.ps1` или `docker-compose ps`

### Порт занят
```powershell
# Найти процесс на порту 8080
netstat -ano | findstr :8080

# Завершить процесс (замените PID на ID процесса)
taskkill /PID <PID> /F
```

### Ошибка подключения к MongoDB
Подождите 30-60 секунд - Replica Set инициализируется при первом запуске.

### Полная переустановка
```powershell
# Со скриптом
.\cleanup-app.ps1
.\start-app.ps1

# Без скрипта
docker-compose down -v
docker-compose up -d
```

### Ошибка "Cannot connect to Docker daemon"
1. Запустите Docker Desktop
2. Дождитесь полной загрузки (иконка в трее перестанет мигать)
3. Повторите попытку

---

## 📊 Мониторинг

### Проверка статуса MongoDB Replica Set
```powershell
docker exec mongo1 mongosh -u admin -p HousingAdmin2024! --authenticationDatabase admin --eval "rs.status()"
```

### Просмотр базы данных
```powershell
# Подключение к MongoDB
docker exec -it mongo1 mongosh -u housingapp -p HousingApp2024! --authenticationDatabase HousingUtilitiesSystemDB

# Внутри mongosh:
use HousingUtilitiesSystemDB
show collections
db.houses.countDocuments()
```

### Резервное копирование
```powershell
# Создание резервной копии
docker exec mongo1 mongodump -u housingapp -p HousingApp2024! --authenticationDatabase HousingUtilitiesSystemDB --archive=/tmp/backup.archive --db=HousingUtilitiesSystemDB
docker cp mongo1:/tmp/backup.archive ./backup.archive

# Восстановление из резервной копии
docker cp ./backup.archive mongo1:/tmp/backup.archive
docker exec mongo1 mongorestore -u housingapp -p HousingApp2024! --authenticationDatabase HousingUtilitiesSystemDB --archive=/tmp/backup.archive
```

---

## 📁 Структура проекта

```
HousingUtilitiesSystemAdmin/
├── src/                          # Исходный код
│   ├── main/java/               # Java код
│   ├── main/resources/          # Ресурсы, шаблоны, конфиги
│   │   ├── application.yml      # Базовая конфигурация
│   │   ├── application-dev.yml  # Конфигурация для разработки
│   │   ├── application-prod.yml # Конфигурация для продакшена
│   │   └── application-docker.yml # Конфигурация для Docker
│   └── test/                    # Тесты
├── sample_data/                 # CSV файлы с тестовыми данными
├── docker-compose.yml           # Конфигурация Docker сервисов
├── Dockerfile                   # Сборка образа приложения
├── mongo-init.js               # Инициализация MongoDB Replica Set
├── start-app.ps1               # Скрипт запуска приложения
├── stop-app.ps1                # Скрипт остановки приложения
├── status-app.ps1              # Скрипт проверки статуса
├── logs-app.ps1                # Скрипт просмотра логов
├── cleanup-app.ps1             # Скрипт полной очистки
└── README.md                   # Этот файл
```

---

## 🔐 Конфигурация

### Переменные окружения

Приложение использует переменные окружения для хранения секретных данных. Для локальной разработки создайте файл `.env` на основе `.env.example`:

```powershell
# Скопируйте пример
Copy-Item .env.example .env

# Отредактируйте .env и укажите ваши значения
```

**Обязательные переменные:**

- `OAUTH_GOOGLE_CLIENT_ID` - Google OAuth Client ID
- `OAUTH_GOOGLE_CLIENT_SECRET` - Google OAuth Client Secret
- `JWT_SECRET` - Секретный ключ для JWT токенов

**Пример `.env` файла:**
```env
OAUTH_GOOGLE_CLIENT_ID=your-google-client-id-here
OAUTH_GOOGLE_CLIENT_SECRET=your-google-client-secret-here
JWT_SECRET=your-jwt-secret-key-here
```

⚠️ **Важно:** Никогда не коммитьте файл `.env` в Git! Он уже добавлен в `.gitignore`.

### Аутентификация MongoDB

**MongoDB Replica Set настроен с аутентификацией пользователей:**

- **Admin пользователь:** `admin` / `HousingAdmin2024!`
- **App пользователь:** `housingapp` / `HousingApp2024!`
- **База данных:** `HousingUtilitiesSystemDB`

⚠️ **Важно:** Эта конфигурация оптимизирована для локальной разработки. Для продакшена добавьте keyFile аутентификацию и смените пароли!

### Локальная разработка (без Docker)
Файл: `src/main/resources/application-dev.yml`
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: HousingUtilitiesSystemDB
      username: admin
      password: "0000"
```

### Docker окружение
Файл: `src/main/resources/application-docker.yml`
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://housingapp:HousingApp2024!@mongo1:27017,mongo2:27017,mongo3:27017/HousingUtilitiesSystemDB?replicaSet=rs0&authSource=HousingUtilitiesSystemDB
```

### Продакшен
Файл: `src/main/resources/application-prod.yml`
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://housingapp:CHANGE_PASSWORD_IN_PRODUCTION@mongo1:27017,mongo2:27017,mongo3:27017/HousingUtilitiesSystemDB?replicaSet=rs0&authSource=HousingUtilitiesSystemDB
```

---

## 📄 Лицензия

[Информация о лицензии]

---

## 👨‍💻 Разработка

### Запуск в IDE (без Docker)

#### Вариант 1: С локальной MongoDB
1. Установите MongoDB локально
2. Запустите MongoDB:
   ```powershell
   mongod --dbpath C:\data\db
   ```
3. Убедитесь, что активен профиль `dev`
4. Запустите `HousingUtilitiesSystemAdminApplication.java`
5. Приложение доступно по адресу http://localhost:8080

#### Вариант 2: С MongoDB в Docker
1. Запустите только MongoDB сервисы:
   ```powershell
   docker-compose up -d mongo1 mongo2 mongo3
   ```
2. Дождитесь инициализации Replica Set (30-60 секунд)
3. Измените профиль на `dev` и обновите connection string в `application-dev.yml`:
   ```yaml
   uri: mongodb://housingapp:HousingApp2024!@localhost:27017,localhost:27018,localhost:27019/HousingUtilitiesSystemDB?replicaSet=rs0&authSource=HousingUtilitiesSystemDB
   ```
4. Запустите приложение в IDE
5. Приложение доступно по адресу http://localhost:8080

### Сборка JAR файла
```powershell
# Сборка с пропуском тестов
mvn clean package -DskipTests

# Запуск JAR файла
java -jar target\HousingUtilitiesSystemAdmin-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Пересборка Docker образа
```powershell
# Пересборка без кэша
docker-compose build --no-cache app

# Запуск с новым образом
docker-compose up -d app
```

### Полезные команды Maven
```powershell
# Компиляция проекта
mvn compile

# Запуск тестов
mvn test

# Очистка проекта
mvn clean

# Проверка зависимостей
mvn dependency:tree
```

---

## 📚 Дополнительная информация

### Переменные окружения для продакшена

Для безопасного развертывания в продакшене используйте переменные окружения:

```yaml
# docker-compose.yml для продакшена
environment:
  - SPRING_PROFILES_ACTIVE=prod
  - JWT_SECRET=your_secure_jwt_secret_here
  - OAUTH_GOOGLE_CLIENT_ID=your_google_client_id
  - OAUTH_GOOGLE_CLIENT_SECRET=your_google_client_secret
  - MONGODB_URI=mongodb://user:password@host:port/database
```

### Логирование

- **DEV**: Подробное логирование (DEBUG уровень)
- **PROD**: Минимальное логирование (INFO/WARN уровень)
- **DOCKER**: DEBUG уровень для отладки

Логи приложения можно просмотреть:
```powershell
# В Docker
docker-compose logs -f app

# Для конкретного контейнера
docker logs -f housing-app

# Последние 100 строк
docker-compose logs --tail=100 app
```

---

**Для быстрого старта используйте:** `.\start-app.ps1` или `docker-compose up -d`

