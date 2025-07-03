CREATE TABLE IF NOT EXISTS users(
    user_id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(250),
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS role(
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(250),
    creator_id BIGINT NOT NULL,
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(250),
    due_date TIMESTAMP NOT NULL,
    status VARCHAR(10),
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE IF NOT EXISTS users_responsability (
    task_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES task(id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS team (
    project_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);