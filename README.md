# Gestão DP

Sistema de Gestão de Departamento Pessoal.

## Estrutura
- `backend/`: API REST em Java e Spring Boot.
- `frontend/`: interface web em React e Vite.

## Tecnologias
- Java 21, Spring Boot, Spring Security, Spring Data JPA, MySQL 8.4, Flyway e Maven.
- React, Vite, JavaScript e Lucide React.

## Requisitos
- JDK 21
- Node.js e npm
- Docker Desktop
- Git

## Executar o backend
Configure as variáveis necessárias no arquivo `.env` dentro de `backend/`. Não versione nem compartilhe esse arquivo.

```bash
cd backend
docker compose up -d
./mvnw spring-boot:run
```

No Windows PowerShell, utilize `.\mvnw.cmd spring-boot:run`.

API local: http://localhost:8080

## Executar o frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend local: http://localhost:5173

## Verificações
Frontend: `npm run lint` e `npm run build`.
Backend: `./mvnw test`.

## Segurança
- Não publique arquivos `.env`, senhas, tokens ou chaves privadas.
- Utilize credenciais fortes e HTTPS em produção.
- Não exponha o banco de dados diretamente à internet.
- Revise autenticação e autorização antes da implantação.

## Status
Projeto em desenvolvimento. A configuração de produção ainda requer revisão.
