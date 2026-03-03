# Devise Center

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white)
![Beta](https://img.shields.io/badge/status-beta-yellow?style=flat-square)

API REST para uma plataforma de posts e comentários, com autenticação JWT e upload de imagens via Cloudinary.

Link para interface de testes da aplicação: https://devise-center-interface.onrender.com

## Funcionalidades

- Cadastro e autenticação de usuários com JWT
- Criação, edição e remoção de posts (com imagem opcional)
- Comentários e respostas em posts (1 nível de profundidade)
- Upload e gerenciamento de foto de perfil
- Filtragem de posts e comentários por autor

## Plataformas & Serviços

- **Banco de dados:** PostgreSQL via [Supabase](https://supabase.com)
- **Armazenamento de imagens:** [Cloudinary](https://cloudinary.com)

## Requisitos

- Java 21+
- Maven
- PostgreSQL (ou conta no Supabase/Neon.tech)
- Conta no Cloudinary

## Instalação

```bash
git clone https://github.com/carlossant77/devise-center.git
cd devise-center
```

Copie o arquivo de exemplo de variáveis de ambiente:

```bash
cp .env-example .env
```

Preencha o `.env` com suas credenciais:

```env
DB_URL=jdbc:postgresql://<host>:<porta>/<banco>
DB_USER=seu_usuario
DB_PASSWORD=sua_senha

CLOUDINARY_NAME=seu_cloud_name
CLOUDINARY_KEY=sua_api_key
CLOUDINARY_SECRET=seu_api_secret

JWT_KEY=sua_chave_secreta
```

Suba a aplicação:

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## Uso

### Autenticação

**Registrar usuário:**
```http
POST /auth/register
Content-Type: application/json

{
  "username": "seunome",
  "email": "seu@email.com",
  "password": "suasenha"
}
```

**Login:**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "seunome",
  "password": "suasenha"
}
```

O token JWT retornado deve ser enviado no header das rotas autenticadas:

```
Authorization: Bearer <token>
```

### Posts

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| GET | `/posts` | ❌ | Listar posts (filtro: `?author=username`) |
| GET | `/posts/{id}` | ❌ | Buscar post por ID |
| POST | `/posts` | ✅ | Criar post (multipart/form-data) |
| PUT | `/posts/{id}` | ✅ | Atualizar post |
| DELETE | `/posts/{id}` | ✅ | Deletar post |

### Comentários

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| GET | `/comments` | ❌ | Listar comentários (filtros: `?author=` e `?post=`) |
| GET | `/comments/{id}` | ❌ | Buscar comentário por ID |
| GET | `/comments/{id}/replies` | ❌ | Listar respostas de um comentário |
| POST | `/comments` | ✅ | Criar comentário ou resposta |
| PUT | `/comments/{id}` | ✅ | Atualizar comentário |
| DELETE | `/comments/{id}` | ✅ | Deletar comentário |

### Usuários

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| GET | `/users` | ❌ | Listar usuários |
| GET | `/users/{id}` | ❌ | Buscar usuário por ID |
| GET | `/users/me` | ✅ | Retorna dados referentes a você mesmo (utiliza o seu token para isso) |
| PUT | `/users/{id}` | ✅ | Atualizar usuário |
| DELETE | `/users/{id}` | ✅ | Deletar usuário |
| PUT | `/users/{id}/pictures` | ✅ | Definir foto de perfil |
| DELETE | `/users/{id}/pictures` | ✅ | Remover foto de perfil |

## Estrutura

```
devise-center/
├── src/
│   ├── main/
│   │   ├── java/br/com/devisecenter/devise_center/
│   │   │   ├── exceptions/         # Exceções customizadas e DTOs de erro
│   │   │   ├── handler/            # Handler global de exceções
│   │   │   ├── infra/
│   │   │   │   ├── configurations/ # Beans (Cloudinary, BCrypt, AuthManager)
│   │   │   │   ├── security/       # Filtro JWT e configuração de segurança
│   │   │   │   └── tasks/          # Keep-alive do banco de dados
│   │   │   └── modules/
│   │   │       ├── auth/           # Login e registro
│   │   │       ├── comments/       # Comentários e respostas
│   │   │       ├── posts/          # Posts com imagem
│   │   │       ├── upload/         # Serviço de upload (Cloudinary)
│   │   │       └── users/          # Usuários e foto de perfil
│   │   └── resources/
│   │       └── application.properties
│   └── test/                       # Testes unitários (Mockito)
├── .env-example
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Testes

```bash
./mvnw test
```

Os testes unitários cobrem os serviços de `User`, `Post`, `Comment` e `UploadFile` usando Mockito.

## Health Check

```http
GET /api/health
```

Retorna `200 OK` com o corpo `"Sistema Ativo"` quando a aplicação está no ar.
