
-- sample data, only executed for in-memory type databases

insert into recipes (id, name, instructions, category, nof_servings)
values(1, 'Spaghetti Bolognese', 'Boil salted water, brown the meat in a saucepan ...', 1, 4),
      (2, 'Chick Pea Curry', 'Soak the peas in water, glaze the onions ...', 4, 2),
      (3, 'Pancakes', 'Mix flour and milk ...', 3, 4);

insert into ingredients (id, name)
values(1, 'Minced Beef'),
      (2, 'Canned Tomatoes'),
      (3, 'Onions'),
      (4, 'Chick Peas'),
      (5, 'Wheat Flour'),
      (6, 'Milk')
;

insert into recipe_ingredient (recipe, ingredient)
values (1, 1),
       (1, 2),
       (2, 2),
       (2, 3),
       (2, 4),
       (3, 5),
       (3, 6) ;

