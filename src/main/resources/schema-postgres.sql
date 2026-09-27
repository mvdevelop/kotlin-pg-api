
-- Script específico para PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela com algumas features específicas do PostgreSQL
CREATE TABLE IF NOT EXISTS produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    preco NUMERIC(10,2) NOT NULL CHECK (preco > 0),
    quantidade INTEGER NOT NULL CHECK (quantidade >= 0),
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_produto_nome UNIQUE (nome)
);

-- Trigger para atualizar data_atualizacao
CREATE OR REPLACE FUNCTION update_data_atualizacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_data_atualizacao
BEFORE UPDATE ON produtos
FOR EACH ROW
EXECUTE FUNCTION update_data_atualizacao();

-- View para produtos ativos
CREATE OR REPLACE VIEW vw_produtos_ativos AS
SELECT id, nome, descricao, preco, quantidade, data_criacao
FROM produtos
WHERE ativo = TRUE;
