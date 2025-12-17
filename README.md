
# 🚀 API Kotlin – Meu Livro de Receitas

API REST desenvolvida em **Kotlin** utilizando **JDK 21**, com documentação interativa via **Swagger (OpenAPI)**.  
O projeto tem como objetivo gerenciar receitas culinárias de forma simples, organizada e escalável.

---

## 🧰 Tecnologias Utilizadas

- **Kotlin**
- **Java 21 (JDK 21)**
- **Spring Boot**
- **Swagger / OpenAPI**
- **Gradle (Kotlin DSL)**
- **Banco de Dados Relacional** (PostgreSQL / MySQL / H2)
- **Docker** (opcional)

---

## 📂 Funcionalidades

- 📌 Cadastro de receitas
- 📌 Listagem de receitas
- 📌 Consulta de receita por ID
- 📌 Atualização de receitas
- 📌 Remoção de receitas
- 📌 Documentação automática com Swagger

---

## 📑 Documentação da API (Swagger)

Após iniciar a aplicação, a documentação estará disponível em:

http://localhost:8080/swagger-ui.html

nginx
Copiar código

ou

http://localhost:8080/swagger-ui/index.html

yaml
Copiar código

---

## ⚙️ Pré-requisitos

Antes de iniciar, você precisará ter instalado:

- **Java JDK 21**
- **Gradle** (ou Gradle Wrapper)
- **Git**

Verifique as versões:
```bash
java -version
gradle -v
▶️ Como Executar o Projeto
1️⃣ Clonar o repositório
bash
Copiar código
git clone https://github.com/seu-usuario/meu-livro-de-receitas.git
cd meu-livro-de-receitas
2️⃣ Executar a aplicação
Usando o Gradle Wrapper (recomendado):

bash
Copiar código
./gradlew bootRun
No Windows:

powershell
Copiar código
gradlew.bat bootRun
A API será iniciada em:

arduino
Copiar código
http://localhost:8080
🔗 Endpoints Principais (Exemplo)
Método	Rota	Descrição
GET	/api/receitas	Listar todas as receitas
GET	/api/receitas/{id}	Buscar receita por ID
POST	/api/receitas	Criar nova receita
PUT	/api/receitas/{id}	Atualizar receita
DELETE	/api/receitas/{id}	Remover receita

🧪 Exemplo de Requisição (POST)
json
Copiar código
{
  "titulo": "Bolo de Chocolate",
  "descricao": "Bolo simples e fofinho",
  "tempoPreparo": 60,
  "ingredientes": [
    "Farinha",
    "Ovos",
    "Chocolate em pó",
    "Açúcar"
  ]
}
🗂️ Estrutura do Projeto (Exemplo)
css
Copiar código
meu-livro-de-receitas/
├── src/main/kotlin
│   └── com/seuprojeto/receitas
│       ├── controller
│       ├── service
│       ├── repository
│       └── model
├── src/main/resources
│   └── application.yml
├── build.gradle.kts
└── README.md
🔒 Boas Práticas
Uso de DTOs para entrada e saída

Validações com Bean Validation

Separação de responsabilidades (Controller, Service, Repository)

Versionamento de API (opcional)

📌 Próximos Passos
Autenticação e autorização (JWT)

Paginação e filtros

Testes automatizados

Deploy em nuvem (Render, Railway, Fly.io)

👨‍💻 Autor
Desenvolvido por mvdevelop
📧 Email: seu-email-aqui
🔗 GitHub: seu-github-aqui

📄 Licença
Este projeto está sob a licença MIT.
Sinta-se à vontade para usar, modificar e contribuir 🚀
