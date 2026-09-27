
-- Tabela de produtos
CREATE TABLE IF NOT EXISTS produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    preco NUMERIC(10,2) NOT NULL,
    quantidade INTEGER NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dados iniciais para desenvolvimento
INSERT INTO produtos (nome, descricao, preco, quantidade) 
VALUES 
    ('Notebook Dell Inspiron', 'Notebook Dell Inspiron 15, 16GB RAM, 512GB SSD, Intel Core i7', 4500.00, 10),
    ('Mouse Logitech MX Master 3', 'Mouse sem fio ergonômico com scroll preciso e múltiplas conexões', 350.00, 50),
    ('Teclado Mecânico Redragon', 'Teclado mecânico com switches Blue e iluminação RGB personalizável', 280.00, 30),
    ('Monitor Samsung 24"', 'Monitor LED 24 polegadas Full HD, 75Hz, painel IPS', 899.99, 25),
    ('Headset Gamer HyperX', 'Headset gamer com som surround 7.1 e microfone removível', 399.99, 40)
ON CONFLICT DO NOTHING;

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_produtos_nome ON produtos(nome);
CREATE INDEX IF NOT EXISTS idx_produtos_preco ON produtos(preco);
