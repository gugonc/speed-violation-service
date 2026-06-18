# speed-violation-service

Microserviço REST para apuração de infrações por excesso de velocidade conforme o **Art. 218 do CTB**. Recebe leituras de equipamentos de fiscalização, aplica a tolerância legal e classifica a gravidade da infração.

Desenvolvido como prova prática para a vaga de **Desenvolvedor FullStack Java** na Velsis Sistemas e Tecnologia Viária.

---

## Aplicação Hospedada

| Serviço | URL |
|---|---|
| API (Railway) | `https://speed-violation-service-production.up.railway.app` |
| Swagger UI | `https://speed-violation-service-production.up.railway.app/swagger-ui.html` |
| Console visual (Vercel) | `https://speed-violation-service.vercel.app` |

### Testando a aplicação hospedada

**Infração Média**:
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":77,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Infração Grave**: 
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Infração Gravíssima**:
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":130,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Velocidade decimal (aceita e arredondada)**:
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":85.5,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Sem infração**:
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: MOBILE" \
  -d '{"licensePlate":"XYZ9876","measuredSpeed":64,"speedLimit":60,"equipmentId":"RAD-CWB-002","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Erro de validação (400)** — placa inválida:
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"INVALIDA","measuredSpeed":80,"speedLimit":60,"equipmentId":"RAD-001","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Erro de validação (400)** — formato de velocidade inválido:
```bash
curl -s -X POST https://speed-violation-service-production.up.railway.app/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":"rapido","speedLimit":60,"equipmentId":"RAD-001","captureTimestamp":"2026-06-17T14:30:00Z"}'
```

**Consultar infrações por placa:**
```bash
curl -s "https://speed-violation-service-production.up.railway.app/api/v1/violations?licensePlate=ABC1D23"
```

---

## Rodando localmente com Docker

### 1. Subir tudo com Docker Compose (recomendado)

Sobe backend + PostgreSQL + frontend de uma vez, a partir da raiz do repositório:

```bash
docker-compose up --build
```

| Serviço | URL |
|---|---|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Console visual | `http://localhost:3000` |

Para derrubar e apagar o volume do banco:

```bash
docker-compose down -v
```

### 2. Subir containers individualmente (sem banco)

O backend sem as variáveis de datasource usa armazenamento em memória automaticamente:

```bash
# A partir da raiz do repositório
docker build -t speed-violation-service ./backend/speed-violation-service
docker build -t speed-violation-console ./frontend/speed-violation-console

# Backend — API disponível em http://localhost:8080 (dados em memória)
docker run -p 8080:8080 speed-violation-service

# Frontend — Console disponível em http://localhost:3000
docker run -e API_URL=http://localhost:8080 -p 3000:3000 speed-violation-console
```

### 3. Rodar os testes (Maven)

```bash
cd backend/speed-violation-service

mvn test      # testes unitários e de integração
mvn verify    # testes + gate de cobertura JaCoCo (≥ 80% na camada de serviço)
```

O relatório de cobertura é gerado em `target/site/jacoco/index.html`.

### 4. Testando com curl (backend local)

```bash
# Infração Média
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":77,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'

# Infração Grave
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'

# Infração Gravíssima
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{"licensePlate":"ABC1D23","measuredSpeed":130,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-17T14:30:00Z"}'

# Sem infração
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: MOBILE" \
  -d '{"licensePlate":"XYZ9876","measuredSpeed":64,"speedLimit":60,"equipmentId":"RAD-CWB-002","captureTimestamp":"2026-06-17T14:30:00Z"}'

# Consultar por placa
curl -s "http://localhost:8080/api/v1/violations?licensePlate=ABC1D23"
```

---

## Documentação técnica

### Stack

| Componente | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3.3.5 |
| Banco de dados | PostgreSQL (gerenciado pelo Railway) |
| Frontend | HTML autocontido (sem framework, sem build tool) |
| Testes | JUnit 5 + MockMvc + JaCoCo |
| Deploy backend | Railway (imagem Docker, CI via GitHub Actions) |
| Deploy frontend | Vercel |

### Regras de negócio (CTB Art. 218)

**Tolerância** — aplicada antes de comparar com o limite:

| Via | Cálculo |
|---|---|
| Limite ≤ 100 km/h | `considerada = medida − 7` |
| Limite > 100 km/h | `considerada = medida × 93 / 100` (inteiro) |

**Classificação** — pelo excesso da velocidade considerada sobre o limite:

| Excesso | Gravidade | Código CTB |
|---|---|---|
| ≤ 20% | MEDIUM (Média) | 218-I |
| > 20% até 50% | SERIOUS (Grave) | 218-II |
| > 50% | VERY_SERIOUS (Gravíssima) | 218-III |

Somente leituras **com infração** são persistidas.

### Arquitetura

```
api/             → HTTP: controller, DTOs, validação, tratamento de erros
domain/model/    → Records e enums (sem dependências de framework)
domain/service/  → Regras de negócio + interface do repositório
infrastructure/  → JdbcViolationRepository (PostgreSQL) | InMemoryViolationRepository (fallback sem banco)
config/          → Tolerância, CORS, Clock, OpenAPI
```

O `SpeedEvaluationService` não conhece HTTP nem JSON — recebe um comando já validado e devolve um resultado. O repositório é uma **interface no domínio**: em produção (Railway) usa `JdbcViolationRepository` com PostgreSQL; em testes e dev local sem banco configurado, o fallback `InMemoryViolationRepository` é ativado automaticamente.

A tabela `violations` é criada pelo **Flyway** na primeira subida do serviço (`V1__create_violations_table.sql`).

### Configuração (variáveis de ambiente)

| Variável | Padrão | Descrição |
|---|---|---|
| `PORT` / `SERVER_PORT` | `8080` | Porta HTTP (Railway injeta `PORT` automaticamente) |
| `SPRING_DATASOURCE_URL` | — | URL JDBC do PostgreSQL (ex.: `jdbc:postgresql://host:5432/db`) |
| `SPRING_DATASOURCE_USERNAME` | — | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | — | Senha do banco |
| `VIOLATION_TOLERANCE_KMH` | `7` | Tolerância fixa em km/h (vias ≤ threshold) |
| `VIOLATION_TOLERANCE_PERCENT` | `7` | Tolerância percentual (vias > threshold) |
| `VIOLATION_TOLERANCE_THRESHOLD_SPEED` | `100` | Limiar entre os dois modos de tolerância |

### Decisões técnicas

**Records sem Lombok** — DTOs e modelos de domínio são `record`s Java 21. Com records o boilerplate já não existe, então Lombok seria redundante.

**Validação manual** — `SpeedReadingValidator` em vez de `@Valid` para garantir os error codes exatos do contrato (`INVALID_LICENSE_PLATE`, `INVALID_ORIGIN` etc.) e a ordem de verificação. Os campos `measuredSpeed` e `speedLimit` aceitam valores decimais (arredondados para inteiro antes da avaliação do CTB). Texto não numérico retorna `INVALID_MEASURED_SPEED` / `INVALID_SPEED_LIMIT` com mensagem "formato inválido"; valor ≤ 0 retorna o mesmo code com "deve ser maior que zero".

**Clock injetável** — `processedAt` e verificação de timestamp futuro usam um bean `Clock`, tornando o comportamento temporal determinístico nos testes.

**Persistência condicional** — `JdbcViolationRepository` ativa quando `SPRING_DATASOURCE_URL` está configurada (condição por propriedade, não por bean); `InMemoryViolationRepository` ativa como fallback quando a propriedade está ausente. Testes continuam sem depender de banco.

**Concorrência segura** — o fallback em memória usa `ConcurrentHashMap<String, CopyOnWriteArrayList<Violation>>` sem locks explícitos; em produção a concorrência é gerenciada pelo próprio PostgreSQL.

**Erros centralizados** — `@RestControllerAdvice` com respostas padronizadas; 4xx logado como `WARN`, 5xx como `ERROR`, sem stack traces expostos ao cliente.
