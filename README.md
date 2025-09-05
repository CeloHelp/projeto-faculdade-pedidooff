# Pedido Offline

Este projeto é uma aplicação de gerenciamento de pedidos desenvolvida em JavaFX, permitindo o registro e a gestão de pedidos de forma offline. Ele foi projetado para ser uma solução simples e eficiente para pequenas empresas ou uso pessoal.

## Funcionalidades

- **Registro de Pedidos**: Adicione novos pedidos com detalhes como produtos, quantidades e informações do cliente.
- **Visualização de Histórico**: Consulte o histórico de pedidos realizados.
- **Exportação de Dados**: Exporte os dados dos pedidos para um arquivo CSV.

## Tecnologias Utilizadas

- **JavaFX**: Para a interface gráfica do usuário.
- **Maven**: Para gerenciamento de dependências e construção do projeto.
- **Mockito**: Para testes unitários e simulação de dependências.
- **JUnit 5**: Framework de testes para Java.

## Como Usar

### Pré-requisitos

Certifique-se de ter o seguinte software instalado em sua máquina:

- **Java Development Kit (JDK) 11 ou superior**
- **Apache Maven 3.6.0 ou superior**

### Configuração do Projeto

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/seu-usuario/pedido-offline.git
   cd pedido-offline
   ```

2. **Compile o projeto com Maven:**

   ```bash
   mvn clean install
   ```

### Executando a Aplicação

Após a compilação bem-sucedida, você pode executar a aplicação a partir da linha de comando:

```bash
java -jar target/pedido-offline-1.0-SNAPSHOT.jar
```

Ou, se estiver usando uma IDE como IntelliJ IDEA ou Eclipse, importe o projeto Maven e execute a classe principal (geralmente `App.java` ou similar).

### Executando os Testes

Para executar os testes unitários do projeto, utilize o Maven:

```bash
mvn test
```

Para gerar um relatório de cobertura de código com JaCoCo:

```bash
mvn test jacoco:report
```

O relatório será gerado em `target/site/jacoco/index.html`.