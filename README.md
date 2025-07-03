![Diagrama](banner.png)
# Teamtacles-API

**Seu gerenciador de tarefas em equipe, direto do fundo do mar. 🦑🌊**

![promocional](teamtacles-gif.gif) 
---
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) 	![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/postgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)


O **Teamtacles-API** é uma API RESTful desenvolvida utilizando **Java, Spring Boot, Flyway para migrações de banco de dados, PostgreSQL como banco de dados e Spring Security com autenticação baseada em JWT**.

A API visa **auxiliar o gerenciamento de tarefas em equipe**, promovendo a colaboração e a produtividade durante a realização de projetos. O **Teamtacles-API** é ideal para times que estão se sentindo **afogados no mar de tarefas** e desejam o **apoio de tentáculos** na organização e gerenciamento, tornando o fluxo de trabalho mais **eficiente e colaborativo**.

## Sobre as profundezas 
- [✨ Funcionalidades Ancoradas](#-funcionalidades-ancoradas)
- [🏝️ Mergulho Local: Como Executar](#-mergulho-local-como-executar)
- [🦑 Tentáculos Autorizados: Como Obter o Token e Testar](#-tentaculos-autorizados-como-obter-o-token-e-testar)
- [🌊 Mapa dos Dados: Estruturas e Validações](#-mapa-dos-dados-estruturas-e-validacoes)
- [🛡️ Rede de Proteção: Autenticação e Autorização](#-rede-de-protecao-autenticacao-e-autorizacao)
- [🌊 Correntes de Testes: Validação das Funcionalidades](#-correntes-de-testes-validacao-das-funcionalidades)

## ✨ Funcionalidades Ancoradas
A documentação completa da API está disponível via Swagger 

🌐 Versão Online (Render): https://teamtacles-api.onrender.com/swagger-ui/index.html

🏠 Localmente: http://localhost:8080/swagger-ui.html

**🔐 Autenticação e Autorização**
- Cadastro de usuários com nome, e-mail e senha;
- Login com geração de token JWT;
- Controle de acesso por papéis: USER e ADMIN.

**🗂️ Gestão de Projetos e Tarefas**
- Criação de projetos;
- CRUD de tarefas vinculadas a projetos: título, descrição, prazo e status;
- Controle de permissões: usuários só editam/excluem suas tarefas;
- Visualização das tarefas dos projetos onde os usuários participam;
- Alteração de status das tarefas (ex: "em andamento" → "concluída");
- Administradores podem visualizar e excluir qualquer tarefa ou projeto;

**📊 Filtros e Relatórios**
- Filtros de tarefa por status, prazo e projeto;
- Listagem de tarefas por status e projeto;
- Consulta de tarefas de usuários específicos para análise de carga de trabalho.

## 🏝️ Mergulho Local: Como Executar

**Pré-requisitos**
- Java JDK 21 ou superior
- Maven 3.6 ou superior
- MySQL

**Build**
1. Clone o repositório:
```bash 
git clone https://github.com/TeamTacles/Teamtacles-API.git
```

2. Configurar as variáveis de ambiente no `application.propierties`

3. Executar o build
```bash
mvn clean install
```

**Run**
1. Executar o projeto
```bash 
mvn spring-boot:run
```

2. Acessar a API em [http://localhost:8080](http://localhost:8080)

## 🦑 Tentáculos Autorizados: Como Obter o Token e Testar

### 1. Registrar um novo usuário
Para criar uma conta nova, envie uma requisição `POST` para o endpoint de registro:
```POST /api/user/register```

**Exemplo de corpo JSON:**

```
{
  "userName": "seu_usuario",
  "email": "seu_usuario@example.com",
  "password": "senhaSegura123",
  "passwordConfirm": "senhaSegura123"
}
```

### 2. Fazer login para obter o token JWT
Depois de registrar (ou usando o usuário ADMIN padrão, se preferir), faça login enviando uma requisição `POST` para:
```POST /api/auth/authenticate```

**Exemplo de corpo JSON:**
```
{
  "userName": "seu_usuario",
  "password": "senhaSegura123"
}
```

Se as credenciais estiverem corretas, você receberá um **token JWT** na resposta:
```
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Testar endpoints protegidos com o token JWT
Para acessar os endpoints que exigem autenticação, envie o token no cabeçalho ```Authorization``` da requisição:
```
Authorization: Bearer <seu_token_aqui>
```

### 4. Usuário ADMIN padrão (para testes)
O projeto já inclui um **usuário ADMIN** criado no banco com as seguintes credenciais:

- Usuário: admin
- Senha: admin123

Você pode usar esse usuário para fazer login e testar a API sem precisar registrar.

**Observação:**
Sempre que o token expirar, faça login novamente para obter um novo token.

## 🌊 Mapa dos Dados: Estruturas e Validações

### 1. User

**Campos:**

- userId (Long) — Gerado automaticamente.
- userName (String) — Obrigatório, tamanho entre 3 e 50 caracteres.
- email (String) — Obrigatório, válido, tamanho entre 8 e 50 caracteres.
- password (String) — Obrigatório, tamanho entre 5 e 100 caracteres.
- task (List\<Task>) — Lista de tarefas que o usuário possui.
- createdProjects (List\<Project>) — Lista de projetos que o usuário criou.
- projects (List\<Project>) — Lista de projetos que o usuário participa.
- roles (Set\<Role>) — Papel (roles) atribuido ao usuário.

**Relacionamentos:**

- 1:N com Task (como dono).
- 1:N com Project (como criador).
- N:M com Project (como membro da equipe).
- N:M com Role.

### 2. Project

**Campos:**

- id (Long) — Gerado automaticamente.
- title (String) — Obrigatório, máximo de 50 caracteres.
- description (String) — Opcional, máximo de 50 caracteres.
- tasks (List\<Task>) — Lista de tarefas associadas.
- creator (User) — Usuário criador (obrigatório).

team — Lista de usuários que participam do projeto.

**Relacionamentos:**
- 1:N com Task.
- N:1 com User (criador).
- N:M com User (equipe).

### 3. Task

**Campos:**

- id (Long) — Gerado automaticamente.
- title (String) — Obrigatório, máximo de 50 caracteres.
- description (String) — Opcional, máximo de 250 caracteres.
- dueDate (LocalDateTime) — Obrigatório, deve ser uma data futura.
- status (Enum Status) — Obrigatório (ver valores possíveis).
- owner (User) — Usuário dono da task (obrigatório).
- usersResponsability (List\<User>) — Lista de usuários responsáveis.
- project (Project) — Projeto associado (obrigatório).

**Relacionamentos:**

- N:1 com User (owner).
- N:M com User (responsáveis).
- N:1 com Project.

**Validações:**
- dueDate precisa ser no futuro.
- title e description com limites de caracteres.

### 4. Role

**Campos:**
- id (Long) — Gerado automaticamente.
- roleName (Enum ERole) — Obrigatório e único (ver valores possíveis).

**Relacionamentos:**
- N:M com User.

### Enumerações
1. ERole - Define os papéis (roles) de um usuário:
    - USER
    - ADMIN

2. Status - Define o status de uma tarefa (Task):
    - TODO — A fazer
    - INPROGRESS — Em andamento
    - DONE — Concluído

**Observações importantes:**
- Chaves primárias são geradas automaticamente (`@GeneratedValue`).

- Validações são feitas via Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Future`).
- Serialização cuida de problemas de referência cíclica com `@JsonManagedReference` e `@JsonBackReference`.
- O modelo utiliza JPA para persistência e Spring Security para autenticação e autorização com UserDetails.

## 🛡️ Rede de Proteção: Autenticação e Autorização

### 🔒 Autenticação
É utilizado o JWT (JSON Web Token) para autenticar os usuários.

- Após o login bem-sucedido, é retornado um token JWT, que deve ser incluído no header de todas as requisições aos endpoints protegidos.
- O token deve ser enviado da seguinte forma:
```
Authorization: Bearer <seu_token_jwt>
```
O usuário precisa estar previamente cadastrado no sistema para realizar o login e obter o token.

### 🛡️ Autorização
O controle de acesso aos endpoints é baseado nos papéis (roles) atribuídos aos usuários:
- **USER:** Acesso a recursos básicos.
- **ADMIN:** Acesso total, incluindo recursos administrativos.

A autorização é feita automaticamente pelo Spring Security, que verifica:
- Se o token JWT é válido.
- Se o usuário possui o papel necessário para acessar determinado endpoint.

### 🔑 Papéis e permissões
**Usuário comum (USER):**
- Pode visualizar e gerenciar suas próprias tarefas e projetos.
- Pode participar de projetos como membro da equipe.

**Administrador (ADMIN):**
- Pode realizar todas as operações do sistema, incluindo criar, atualizar e excluir qualquer recurso.

## 🌊 Correntes de Testes: Validação das Funcionalidades
Foram implementados testes unitários e funcionais para validar as principais funcionalidades da aplicação. Estes testes garantem que as rotas da API de usuários se comportam corretamente em diferentes cenários, simulando o fluxo real da aplicação, incluindo autenticação e autorização.

### 🧪 Testes Unitários

### UserServiceTest
**Cenários Testados**
**📝 Registro de Usuário**
- ✅ Deve permitir o registro de um novo usuário com dados válidos, retornando o DTO do usuário criado.
- ❌ Deve lançar exceção `UsernameAlreadyExistsException` se o username já existir.
- ❌ Deve lançar exceção `EmailAlreadyExistsException` se o e-mail já estiver cadastrado.
- ❌ Deve lançar exceção `PasswordMismatchException` se as senhas não coincidirem.

**🔄 Atualização de Papel (Role) do Usuário**
- ✅ Deve atualizar o papel do usuário com sucesso e retornar o DTO atualizado.
- ❌ Deve lançar exceção `ResourceNotFoundException` se o usuário não existir.
- ❌ Deve lançar exceção `IllegalArgumentException` se o papel informado não for válido.

### ProjectServiceTest
**Cenários Testados**
**📝 Criação de Projeto**
- ✅ Deve criar um projeto com dados válidos, retornando o DTO do projeto criado.
- ❌ Deve lançar `ResourceNotFoundException` quando algum usuário da equipe não for encontrado.

**🔍 Consulta de Projeto**
- ✅ Deve retornar um projeto pelo ID quando ele existir.
- ❌ Deve lançar `ResourceNotFoundException` quando o projeto não for encontrado pelo ID.

**✏️ Atualização Completa de Projeto**
- ✅ Deve atualizar um projeto quando o usuário for o dono.
- ✅ Deve atualizar um projeto quando o usuário for ADM.
- ❌ Deve lançar `InvalidTaskStateException` quando um usuário não-dono e não-ADM tentar atualizar o projeto.

**✏️ Atualização Parcial de Projeto (Patch)**
- ✅ Deve atualizar parcialmente um projeto quando o usuário for o dono.
- ✅ Deve atualizar parcialmente um projeto quando o usuário for ADM.
- ❌ Deve lançar `InvalidTaskStateException` quando um usuário não-dono e não-ADM tentar atualizar parcialmente o projeto.

**🗑 Exclusão de Projeto**
- ✅ Deve deletar um projeto quando o usuário for o dono.
- ✅ Deve deletar um projeto quando o usuário for ADM.
- ❌ Deve lançar `InvalidTaskStateException` quando um usuário não-dono e não-ADM tentar deletar o projeto.

### TaskServiceTest
**Cenários Testados**
**📝 Criação de Tarefa**
- ✅ Deve criar uma tarefa com sucesso quando os dados forem válidos.
- ❌ Deve lançar `ResourceNotFoundException` quando o projeto não existir.
- ❌ Deve lançar `ResourceNotFoundException` quando o usuário responsável não existir.

**🔍 Consulta de Task por ID**
- ✅ Deve retornar a task pelo ID quando o usuário for admin.
- ✅ Deve retornar a task pelo ID quando o usuário for o dono (owner).
- ✅ Deve retornar a task pelo ID quando o usuário for responsável.
- ❌ Deve lançar `ResourceNotFoundException` quando a task não existir pelo ID.
- ❌ Deve lançar `ResourceNotFoundException` quando a task não pertencer ao projeto especificado.
- ❌ Deve lançar `InvalidTaskStateException` quando usuário não autorizado tentar acessar a task.

**📋 Consulta de Tasks de Usuário em Projeto**
- ✅ Deve retornar lista paginada de tasks de um usuário em um projeto quando quem acessa for admin.
- ❌ Deve lançar `AccessDeniedException` quando usuário não admin tentar acessar tasks de outro usuário.
- ❌ Deve lançar `ResourceNotFoundException` quando o projeto não existir (no acesso do admin).
- ❌ Deve lançar `ResourceNotFoundException` quando usuário alvo da busca não for encontrado (acesso admin).
- ✅ Deve retornar página vazia quando usuário alvo não tiver tarefas no projeto (acesso admin).

**📋 Buscar Todas as Tarefas com Filtros**
- ✅ Deve retornar todas as tarefas quando nenhum filtro for aplicado (acesso admin).
- ✅ Deve retornar tarefas filtradas corretamente quando todos os filtros forem aplicados (acesso admin).
- ❌ Deve lançar `ResourceNotFoundException` ao filtrar por projeto inexistente (acesso admin).
- ❌ Deve lançar `IllegalArgumentException` quando o admin filtrar por um status inválido.
- ✅ Deve retornar as tarefas do usuário normal sem filtros aplicados.
- ✅ Deve retornar as tarefas do usuário normal filtrando por projeto válido.
- ❌ Deve lançar `AccessDeniedException` quando o usuário normal tentar filtrar por projeto do qual não faz parte.
- ✅ Deve retornar as tarefas do usuário normal filtrando por status válido.

### ⚙️ Testes Funcionais
Os testes funcionais utilizam o MockMvc para simular requisições HTTP reais à API, garantindo a validação completa dos endpoints. Para facilitar os testes, implementamos utilitários que geram tokens JWT para os perfis de usuário comum e administrador.

Além disso, realizamos o isolamento dos testes com o uso do `@BeforeEach`, garantindo um ambiente limpo e consistente a cada execução, evitando interferência entre os casos de teste.

#### UserControllerTest

**Cenários Testados**

**📝 Registro de Usuário**
- ✅ Deve permitir o registro de um novo usuário, retornando 201 Created.
- ❌ Deve retornar 409 Conflict se o username já existir.
- ❌ Deve retornar 409 Conflict se o e-mail já estiver cadastrado.
- ❌ Deve retornar 400 Bad Request se as senhas não coincidirem.

**🔐 Atualização de Permissão**
- ✅ Deve, como ADMIN, permitir atualizar a permissão (role) de um usuário, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden se um usuário comum tentar alterar permissões.
- ❌ Deve retornar 400 Bad Request se a role informada for inválida.

**📄 Listagem de Usuários**
- ✅ Deve, como ADMIN, consultar a lista paginada de todos os usuários.
- ❌ Deve retornar 403 Forbidden ao tentar listar usuários sem perfil de administrador.
- ❌ Deve retornar 401 Unauthorized para usuários não autenticados.

### ProjectControllerTest
**Cenários Testados**

**🆕 Criação de Projeto**
- ✅ Deve, como USER/ADMIN comum, criar um projeto, retornando 201 Created.
- ❌ Deve retornar 404 Not Found quando algum usuário da equipe não for encontrado.
- ❌ Deve retornar 400 Bad Request se o campo obrigatório "título" estiver vazio.
- ❌ Deve retornar 400 Bad Request se o campo obrigatório "time" estiver vazio.

**📋 Listagem de Projetos**
- ✅ Deve, como ADMIN, listar todos os projetos, retornando 200 OK.
- ✅ Deve, como USER, listar os projetos que participa.
- ✅ Deve, como ADMIN, listar qualquer projeto existente filtrado pelo ID, retornando 200 OK.
- ❌ Deve retornar 404 Not Found quando o projeto não for encontrado pelo ID.
- ❌ Deve retornar 403 Forbidden quando o usuário tentar acessar projeto pelo ID do qual não faz parte.

**✏️ Atualização Parcial (PATCH)**
- ✅ Deve, como ADMIN, permitir atualização parcial do projeto, retornando 200 OK.
- ✅ Deve permitir que o usuário dono atualize parcialmente o projeto, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden para usuário comum da equipe ao tentar atualização parcial.
- ❌ Deve retornar 403 Forbidden para usuário comum que não está na equipe ao tentar atualização parcial.

**📝 Atualização Completa (PUT)**
- ✅ Deve, como ADMIN, permitir atualização completa do projeto, retornando 200 OK.
- ✅ Deve permitir que usuário dono atualize completamente o projeto, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden para usuário comum que não é criador ao tentar atualização completa.

**🗑️ Exclusão de Projeto (DELETE)**
- ✅ Deve, como ADMIN, excluir projeto, retornando 204 No Content.
- ✅ Deve permitir que usuário dono exclua seus projetos, retornando 204 No Content.
- ❌ Deve retornar 403 Forbidden para usuário comum que não é criador ao tentar exclusão.

### TaskControllerTest
**Cenários Testados**

**🆕 Criação de Task**
- ✅ Deve criar uma task e retornar 201 Created.
- ❌ Deve retornar 404 Not Found quando o projeto não existir.
- ❌ Deve retornar 404 Not Found quando o ID do usuário responsável não existir.

**📄 Consulta de Task por ID**
- ✅ Deve, como ADMIN, buscar task pelo ID, retornando 200 OK.
- ✅ Deve, como USER responsável, buscar task pelo ID, retornando 200 OK.
- ✅ Deve, como USER criador, buscar task pelo ID, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden para usuário não responsável pela task.
- ❌ Deve retornar 404 Not Found quando a task não existir pelo ID.
- ❌ Deve retornar 404 Not Found quando a task não pertencer ao projeto

**👥 Listagem de Tasks por Projeto e Usuário**
- ✅ Deve, como ADMIN, listar tasks de um usuário de forma paginada, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden quando usuário não admin tentar acessar tasks de outro usuário.
- ✅ Deve, como USER, listar suas próprias tasks de forma paginada, retornando 200 OK.
- ❌ Deve retornar 404 Not Found quando o projeto não existir.
- ❌ Deve retornar 404 Not Found quando o usuário não existir.

**📋 Buscar Todas as Tarefas com Filtros**
- ✅ Deve, como ADMIN, listar todas as tarefas quando nenhum filtro for aplicado.
- ✅ Deve, como USER, listar todas suas tarefas quando nenhum filtro for aplicado.
- ✅ Deve listar todas as tarefas filtradas corretamente quando todos os filtros forem aplicados.
- ❌ Deve retornar 404 Not Found quando o projeto não existir.
- ❌ Deve retornar 400 Bad Request ao filtrar por status inexistente.
- ❌ Deve retornar 403 Forbidden quando usuário normal tentar filtrar por projeto do qual não faz parte.
- ✅ Deve, como USER, retornar suas tarefas filtrando por status válido.

**✏️ Atualização Parcial (PATCH)**
- ✅ Deve, como ADMIN, atualizar parcialmente o status da task, retornando 200 OK.
- ✅ Deve, como USER responsável, atualizar parcialmente o status da task, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden quando usuário não responsável tentar atualizar parcialmente a task.

**📝 Atualização Completa (PUT)**
- ✅ Deve, como ADMIN, atualizar completamente uma task, retornando 200 OK.
- ✅ Deve, como USER responsável, atualizar completamente sua task, retornando 200 OK.
- ❌ Deve retornar 403 Forbidden para usuário não responsável ao tentar atualizar a task.

**🗑️ Exclusão de Task (DELETE)**
- ✅ Deve, como ADMIN, deletar uma task, retornando 204 No Content.
- ✅ Deve, como USER responsável, deletar sua task, retornando 204 No Content.
- ❌ Deve retornar 403 Forbidden para usuário não responsável ao tentar deletar task.

## 🐙 Conheça os Desenvolvedores

O Teamtacles-API foi desenvolvido pelas habilidades e dedicação de nossa equipe:

---

### Caio Dib
* **GitHub:** 🐙 [`@dib10`](https://github.com/dib10)

---

### Gabriela Santana
* **GitHub:** 🐙 [`@GabihSantana`](https://github.com/GabihSantana)

---

### Pedro Lauton
* **GitHub:** 🐙 [`@PedroLauton`](https://github.com/PedroLauton)

---


