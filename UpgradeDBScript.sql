
﻿-- Add courseID field to Question table
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE Question ADD COLUMN courseID integer;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column courseID already exists in Question.';
        END;
    END;
$$
;

-- Add courseID field to QuestionCategory table
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE QuestionCategory ADD COLUMN courseID integer;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column courseID already exists in QuestionCategory.';
        END;
    END;
$$
;

-- Add courseID field to Quiz table
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE Quiz ADD COLUMN courseID integer;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column courseID already exists in Quiz.';
        END;
    END;
$$
;


-- Add courseID field to Package table
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE Package ADD COLUMN courseID integer;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column courseID already exists in Package.';
        END;
    END;
$$
;


-- Add PackageScopeRule table
CREATE TABLE IF NOT EXISTS PackageScopeRule 
(
  packageID integer NOT NULL,
  scope text,
  scopeID text,
  visibility boolean,
  isDefault boolean
) WITH (
  OIDS=FALSE
);

--ALTER TABLE PackageScopeRule ADD CONSTRAINT PackageScopeRule_fk1 FOREIGN KEY (packageID) REFERENCES package(id) ON DELETE CASCADE;
--END


-- Add PlayerScopeRule table
CREATE TABLE IF NOT EXISTS PlayerScopeRule
(
  playerID text,
  scope text
) WITH (
  OIDS=FALSE

);

-- Add Course table
CREATE TABLE IF NOT EXISTS Course
(
  courseID integer NOT NULL,
  userID integer NOT NULL,
  grade text,
  comment text
) WITH (
  OIDS=FALSE
);

ALTER TABLE Course ADD CONSTRAINT Course_fk1 FOREIGN KEY (userID) REFERENCES LMSUser(id) ON DELETE CASCADE;




