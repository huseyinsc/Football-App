-- Create ReservationParticipant junction table for multi-user support
CREATE TABLE reservation_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_organizer BOOLEAN NOT NULL DEFAULT false,
    is_approved BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(reservation_id, user_id)
);

-- Add foreign key for organizer in reservations table
ALTER TABLE reservations
ADD COLUMN organizer_id UUID;

-- Migrate existing data: set organizer_id from user_id
UPDATE reservations SET organizer_id = user_id WHERE organizer_id IS NULL;

-- Add NOT NULL constraint after migration
ALTER TABLE reservations
ALTER COLUMN organizer_id SET NOT NULL;

-- Rename old user_id column to avoid confusion (optional, keeps backward compatibility)
ALTER TABLE reservations
DROP CONSTRAINT IF EXISTS fk_user;

-- Drop old user_id column
ALTER TABLE reservations
DROP COLUMN IF EXISTS user_id;

-- Add indexes for performance
CREATE INDEX idx_reservation_participants_reservation_id ON reservation_participants(reservation_id);
CREATE INDEX idx_reservation_participants_user_id ON reservation_participants(user_id);
CREATE INDEX idx_reservations_organizer_id ON reservations(organizer_id);