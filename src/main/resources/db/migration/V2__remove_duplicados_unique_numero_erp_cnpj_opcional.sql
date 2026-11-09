-- V2: corrige os problemas de integridade identificados na V1

-- 1. Remove pedidos duplicados por numero_erp, mantendo o de menor id.
--    No seed, é o pedido 10047, gravado duas vezes (ids 6 e 7) por causa da nova
--    tentativa após o timeout. Os itens do duplicado são removidos antes, por causa da FK.
DELETE FROM itens_pedido
WHERE pedido_id IN (
    SELECT p.id
    FROM pedidos p
    WHERE EXISTS (
        SELECT 1 FROM pedidos o
        WHERE o.numero_erp = p.numero_erp AND o.id < p.id
    )
);

DELETE FROM pedidos p
WHERE EXISTS (
    SELECT 1 FROM pedidos o
    WHERE o.numero_erp = p.numero_erp AND o.id < p.id
);

-- 2. Garante no banco a idempotência por numero_erp.
ALTER TABLE pedidos
    ADD CONSTRAINT uk_pedidos_numero_erp UNIQUE (numero_erp);

-- 3. Pedidos de consumidor final não têm CNPJ (ex.: 10045).
ALTER TABLE pedidos
    ALTER COLUMN cnpj DROP NOT NULL;
