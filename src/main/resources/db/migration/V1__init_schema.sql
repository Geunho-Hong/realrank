-- V1__init_schema.sql
CREATE TABLE tbl_product
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    product_code VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NULL,
    price        DECIMAL(19, 2) NULL, -- 정밀도 추가 추천
    category     VARCHAR(255) NULL,
    status       VARCHAR(255) NULL,
    description  VARCHAR(255) NULL,
    CONSTRAINT pk_tbl_product PRIMARY KEY (id)
);

ALTER TABLE tbl_product
    ADD CONSTRAINT uc_tbl_product_product_code UNIQUE (product_code);

CREATE TABLE tbl_product_metrics
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    stat_date      VARCHAR(255)          NULL,
    view_count     BIGINT                NOT NULL,
    like_count     BIGINT                NOT NULL,
    last_viewed_at datetime              NULL,
    last_liked_at  datetime              NULL,
    product_code   VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_tbl_product_metrics PRIMARY KEY (id)
);

ALTER TABLE tbl_product_metrics
    ADD CONSTRAINT FK_TBL_PRODUCT_METRICS_ON_PRODUCT_CODE FOREIGN KEY (product_code) REFERENCES tbl_product (product_code);