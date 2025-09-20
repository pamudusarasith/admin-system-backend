ALTER TABLE letters
    DROP CONSTRAINT IF EXISTS chk_assigned_division;
ALTER TABLE letters
    DROP CONSTRAINT IF EXISTS chk_assigned_officer;

ALTER TABLE letters
    ADD CONSTRAINT chk_assigned_division CHECK (
        (assigned_division_id IS NULL AND
         status IN ('NEW', 'RETURNED_FROM_DIVISION', 'CLOSED') AND
         assigned_user_id IS NULL) OR
        (assigned_division_id IS NOT NULL AND
         status IN
         ('ASSIGNED_TO_DIVISION', 'PENDING_ACCEPTANCE', 'ASSIGNED_TO_OFFICER', 'RETURNED_FROM_OFFICER', 'CLOSED'))
        );

ALTER TABLE letters
    ADD CONSTRAINT chk_assigned_officer CHECK (
        (assigned_user_id IS NULL AND
         status IN ('NEW', 'ASSIGNED_TO_DIVISION', 'RETURNED_FROM_DIVISION', 'RETURNED_FROM_OFFICER', 'CLOSED')) OR
        (assigned_user_id IS NOT NULL AND
         status IN ('PENDING_ACCEPTANCE', 'ASSIGNED_TO_OFFICER', 'CLOSED'))
        );