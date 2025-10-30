# SQL utilizado no tabalho
-- Criar base e usar
CREATE DATABASE IF NOT EXISTS arquitetura;
  
USE arquitetura;


CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS dados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL UNIQUE,           
    cpf_cnpj VARCHAR(30),                     
    email VARCHAR(100) UNIQUE,                
    telefone VARCHAR(30),                     
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dados_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS endereco (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    tipo ENUM('residencial','comercial','entrega','outro') DEFAULT 'residencial',
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(100),
    cep VARCHAR(20),
    pais VARCHAR(100) DEFAULT 'Brasil',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_endereco_cliente (cliente_id),
    INDEX idx_endereco_cep (cep)
);


CREATE INDEX idx_dados_email ON dados(email);
CREATE INDEX idx_dados_telefone ON dados(telefone);

-- ===========================
-- DADOS DE TESTE (seed)
-- ===========================
INSERT INTO cliente (nome)
VALUES
  ('Ana Silva'),
  ('João Souza');

INSERT INTO dados (cliente_id, cpf_cnpj, email, telefone)
VALUES
  (1, '123.456.789-00', 'ana.silva@example.com', '+55 11 99999-0001'),
  (2, '12.345.678/0001-99', 'joao.souza@example.com', '+55 21 98888-0002');

INSERT INTO endereco (cliente_id, tipo, logradouro, numero, complemento, bairro, cidade, estado, cep)
VALUES
  (1, 'residencial', 'Rua das Flores', '123', 'Apto 12', 'Centro', 'São Paulo', 'SP', '01001-000'),
  (1, 'entrega', 'Av. Paulista', '2000', NULL, 'Bela Vista', 'São Paulo', 'SP', '01310-200'),
  (2, 'comercial', 'Rua do Comércio', '45', 'Sala 5', 'Centro', 'Rio de Janeiro', 'RJ', '20010-000');


# comandos 
cd "C:\Users\Pedro\Documents\APS - 2025-02 - 23-09-2025\APS - 2025-02 - 23-09-2025\src"
$files = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d ..\bin $files

# ubuntu (wsl)
mysql -u root -p