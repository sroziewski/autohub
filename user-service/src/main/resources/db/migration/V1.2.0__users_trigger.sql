CREATE OR REPLACE FUNCTION ensure_single_default_address()
    RETURNS TRIGGER AS $$
BEGIN
    -- If this is not a default address, no action needed
    IF NOT NEW.is_default THEN
        RETURN NEW;
    END IF;

    -- If this is a default address, set all other addresses for this user to non-default
    UPDATE autohub.addresses
    SET is_default = FALSE
    WHERE user_id = NEW.user_id
      AND id != NEW.id
      AND is_default = TRUE;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ensure_single_default_address
    BEFORE INSERT OR UPDATE ON autohub.addresses
    FOR EACH ROW
EXECUTE FUNCTION ensure_single_default_address();
