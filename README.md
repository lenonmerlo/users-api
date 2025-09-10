# Users API (Spring Boot)

API REST simples para **gestão de usuários** — focada em demonstrar `@RestController`, desenho de endpoints REST e uso correto de **HTTP Status Codes**. Implementação **in-memory** (sem banco), com validações e tratamento de erros padronizado.

> Java 21 • Spring Boot 3.5.x • Maven

---

## 🚀 Como rodar

Requisitos: Java 21 instalado.

```bash
# modo desenvolvimento
./mvnw spring-boot:run

# ou empacotar e rodar o .jar
./mvnw -q -DskipTests package
java -jar target/users-api-0.0.1-SNAPSHOT.jar
```

- Porta padrão: **8080**
- Health check: `GET http://localhost:8080/ping` → `200 OK`

---

## 🧭 Endpoints

Recurso principal: `/users`

| Método | Rota              | Descrição                         |
|-------:|-------------------|-----------------------------------|
|   GET  | `/users`          | Lista usuários                    |
|   GET  | `/users/{id}`     | Detalhe por ID                    |
|  POST  | `/users`          | Cria usuário                      |
|   PUT  | `/users/{id}`     | Atualização **completa**          |
| PATCH  | `/users/{id}`     | Atualização **parcial**           |
| DELETE | `/users/{id}`     | Remove usuário                    |

**Formato:** JSON (`Content-Type: application/json`)

---

## ✅ Regras & validação

- **Obrigatórios (POST/PUT):** `name`, `email`, `role`
- `email` válido e **único** (case-insensitive)
- `role` permitido: **`ADMIN`**, **`USER`**, **`MANAGER`** (case-insensitive; espaços ignorados)
- Entradas sofrem **trim** antes de validar
- **PATCH** aceita campos **opcionais** (somente os enviados são aplicados)

---

## 📬 Status Codes (mapa + motivo)

| Operação              | Sucesso                             | Erros (principais)                                                                                                 |
|-----------------------|-------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `GET /users`          | **200 OK**                          | —                                                                                                                   |
| `GET /users/{id}`     | **200 OK**                          | **404 Not Found** — ID não existe                                                                                   |
| `POST /users`         | **201 Created** + `Location`        | **400 Bad Request** (Bean Validation), **409 Conflict** (e-mail em uso), **422 Unprocessable Entity** (regra/role) |
| `PUT /users/{id}`     | **200 OK** (retorna atualizado)     | **404** (não existe), **409** (e-mail de outro usuário), **422** (regra), **400** (payload inválido)               |
| `PATCH /users/{id}`   | **200 OK**                          | **404**, **409**, **422**                                                                                           |
| `DELETE /users/{id}`  | **204 No Content**                  | **404 Not Found**                                                                                                   |

**Padrão de erro (JSON):**
```json
// 400 (Bean Validation)
{ "error": "BAD_REQUEST", "details": [ { "field": "email", "message": "must be a well-formed email address" } ] }

// 404
{ "error": "NOT_FOUND", "message": "User 999 not found" }

// 409
{ "error": "CONFLICT", "message": "Email already in use" }

// 422 (regra de negócio)
{ "error": "VALIDATION_ERROR", "message": "Role must be one of: [ADMIN, USER, MANAGER]" }
```

---

## 🧪 Exemplos rápidos (cURL)

**Criar**  
```bash
curl -i -X POST http://localhost:8080/users   -H "Content-Type: application/json"   -d '{"name":"Lenon","email":"lenon@example.com","role":"ADMIN"}'
# → 201 Created + Location: /users/1
```

**Listar**  
```bash
curl -s http://localhost:8080/users
# → [{"id":1,"name":"Lenon","email":"lenon@example.com","role":"ADMIN"}]
```

**Buscar por ID**  
```bash
curl -i http://localhost:8080/users/1   # → 200 OK
curl -i http://localhost:8080/users/999 # → 404 Not Found
```

**Atualizar (PUT)**  
```bash
curl -i -X PUT http://localhost:8080/users/1   -H "Content-Type: application/json"   -d '{"name":"Lenon Merlo","email":"lenon_merlo@example.com","role":"MANAGER"}'
# → 200 OK (objeto atualizado)
```

**Atualizar parcial (PATCH)**  
```bash
curl -i -X PATCH http://localhost:8080/users/1   -H "Content-Type: application/json"   -d '{"role":"USER"}'
# → 200 OK
```

**Excluir**  
```bash
curl -i -X DELETE http://localhost:8080/users/1  # → 204 No Content
curl -i http://localhost:8080/users/1            # → 404 Not Found
```

---

## 🧱 Estrutura principal

```
src/main/java/com/lenon/users/
├─ UsersApiApplication.java
├─ controller/
│  ├─ HealthController.java
│  └─ UserController.java
├─ dto/
│  ├─ UserDTO.java
│  ├─ UserRequest.java         # POST/PUT (campos obrigatórios)
│  └─ UserPatchRequest.java    # PATCH (campos opcionais)
├─ service/
│  ├─ UserService.java
│  └─ InMemoryUserService.java # armazenamento em memória (ConcurrentHashMap)
├─ exception/
│  ├─ ApiExceptionHandler.java # 400/404/409/422
│  ├─ ConflictException.java
│  ├─ DomainValidationException.java
│  └─ NotFoundException.java
└─ resources/
   └─ application.properties
```

**Decisões**
- `@RestController` para JSON direto no corpo (sem views)
- Armazenamento **in-memory** (demo/mentoria), dados não persistem entre reinícios
- Entradas sanitizadas (trim) e `role` normalizada para UPPERCASE
- Status codes alinhados a boas práticas REST

---

## 🧰 Testes

Executar testes unitários (service):
```bash
./mvnw clean test
```
Arquivo principal de teste:
```
src/test/java/com/lenon/users/service/InMemoryUserServiceTest.java
```
Cobre: criação, busca por ID, atualização, exclusão, conflito de e-mail, regras de negócio (role).

---

## 📌 Próximos passos (opcionais)
- Migrar storage para **JPA + H2** (ou PostgreSQL)
- Adicionar paginação/filtros em `GET /users`
- Tests de controller com `@WebMvcTest` (verificar **status codes** e **Location**)
- Dockerfile e GitHub Actions

---

Feito com ♥ para mentoria/portfolio.
