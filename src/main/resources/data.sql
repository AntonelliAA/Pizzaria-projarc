-- Inserção dos clientes
INSERT INTO clientes (cpf, nome, celular, endereco, email, senha) VALUES ('9001', 'Huguinho Pato', '51985744566', 'Rua das Flores, 100', 'huguinho.pato@email.com', 'senha');
INSERT INTO clientes (cpf, nome, celular, endereco, email, senha) VALUES ('9002', 'Luizinho Pato', '5199172079', 'Av. Central, 200', 'zezinho.pato@email.com', 'senha');

-- Inserção dos ingredientes
INSERT INTO ingredientes (id, descricao) VALUES (1, 'Disco de pizza');
INSERT INTO ingredientes (id, descricao) VALUES (2, 'Porcao de tomate');
INSERT INTO ingredientes (id, descricao) VALUES (3, 'Porcao de mussarela');
INSERT INTO ingredientes (id, descricao) VALUES (4, 'Porcao de presunto');
INSERT INTO ingredientes (id, descricao) VALUES (5, 'Porcao de calabresa');
INSERT INTO ingredientes (id, descricao) VALUES (6, 'Molho de tomate (200ml)');
INSERT INTO ingredientes (id, descricao) VALUES (7, 'Porcao de oregano');

-- Inserção dos itens de estoque
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (1, 30, 1);
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (2, 30, 2);
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (3, 30, 3);
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (4, 30, 4);
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (5, 30, 5);
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (6, 30, 6);
INSERT INTO itensEstoque (id, quantidade, ingrediente_id) VALUES (7, 30, 7);

-- Inserção das receitas 
INSERT INTO receitas (id, titulo) VALUES (1, 'Pizza calabresa');
INSERT INTO receitas (id, titulo) VALUES (2, 'Pizza queijo e presunto');
INSERT INTO receitas (id, titulo) VALUES (3, 'Pizza margherita');

-- Associação dos ingredientes à receita Pizza calabresa
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (1, 1); -- Disco de pizza
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (1, 6); -- Molho de tomate (200ml)
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (1, 3); -- Porcao de mussarela
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (1, 5); -- Porcao de calabresa
-- Associação dos ingredientes à receita Pizza queijo e presunto
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (2, 1); -- Disco de pizza
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (2, 6); -- Molho de tomate (200ml)
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (2, 3); -- Porcao de mussarela
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (2, 4); -- Porcao de presunto
-- Associação dos ingredientes à receita Pizza margherita
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (3, 1); -- Disco de pizza
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (3, 6); -- Molho de tomate (200ml)
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (3, 3); -- Porcao de mussarela
INSERT INTO receita_ingrediente (receita_id, ingrediente_id) VALUES (3, 7); -- Porcao de oregano

-- insercao dos produtos
INSERT INTO produtos (id,descricao,preco) VALUES (1,'Pizza calabresa',5500);
INSERT INTO produtos (id,descricao,preco) VALUES (2,'Pizza queijo e presunto',6000);
INSERT INTO produtos (id,descricao,preco) VALUES (3,'Pizza margherita',4000);

-- Associação dos produtos com as receitas
INSERT INTO produto_receita (produto_id,receita_id) VALUES(1,1);
INSERT INTO produto_receita (produto_id,receita_id) VALUES(2,2);
INSERT INTO produto_receita (produto_id,receita_id) VALUES(3,3);

-- Insercao dos cardapios
INSERT INTO cardapios (id,titulo) VALUES(1,'Cardapio de Agosto');
INSERT INTO cardapios (id,titulo) VALUES(2,'Cardapio de Setembro');

-- Associação dos cardapios com os produtos
INSERT INTO cardapio_produto (cardapio_id,produto_id) VALUES (1,1);
INSERT INTO cardapio_produto (cardapio_id,produto_id) VALUES (1,2);
INSERT INTO cardapio_produto (cardapio_id,produto_id) VALUES (1,3);

INSERT INTO cardapio_produto (cardapio_id,produto_id) VALUES (2,1);
INSERT INTO cardapio_produto (cardapio_id,produto_id) VALUES (2,3);

-- ─── Pedidos entregues para testes de UC8 e UC9 ───────────────────────
INSERT INTO pedidos (id, cliente_cpf, data_hora_criacao, endereco_entrega, status, valor, impostos, desconto, valor_cobrado)
VALUES (1, '9001', TIMESTAMP '2026-05-20 18:30:00', 'Rua das Flores, 100', 'ENTREGUE', 11000, 1100, 0, 12100);

INSERT INTO itens_pedido (id, pedido_id, produto_id, quantidade)
VALUES (1, 1, 1, 2);

INSERT INTO pedidos (id, cliente_cpf, data_hora_criacao, endereco_entrega, status, valor, impostos, desconto, valor_cobrado)
VALUES (2, '9001', TIMESTAMP '2026-05-22 20:15:00', 'Rua das Flores, 100', 'ENTREGUE', 6000, 600, 0, 6600);

INSERT INTO itens_pedido (id, pedido_id, produto_id, quantidade)
VALUES (2, 2, 2, 1);

INSERT INTO pedidos (id, cliente_cpf, data_hora_criacao, endereco_entrega, status, valor, impostos, desconto, valor_cobrado)
VALUES (3, '9002', TIMESTAMP '2026-05-21 12:00:00', 'Av. Central, 200', 'ENTREGUE', 4000, 400, 0, 4400);

INSERT INTO itens_pedido (id, pedido_id, produto_id, quantidade)
VALUES (3, 3, 3, 1);

-- Ajuste de sequências para evitar colisões com IDs inseridos manualmente
ALTER TABLE pedidos ALTER COLUMN id RESTART WITH 100;
ALTER TABLE itens_pedido ALTER COLUMN id RESTART WITH 100;