create table category (
    category_id serial primary key,
    name varchar(255) unique not null
);

-- the mapping table which maps task_install to category 
create table task_install_category (
    lsid varchar(512) not null references task_install(lsid) on delete cascade,
    category_id integer not null references category(category_id) on delete cascade
);

commit;
