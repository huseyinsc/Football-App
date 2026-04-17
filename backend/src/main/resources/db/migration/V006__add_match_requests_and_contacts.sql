-- Remove is_approved if it exists
ALTER TABLE reservation_participants DROP COLUMN IF EXISTS is_approved;

-- Add join_policy to reservations
ALTER TABLE reservations 
ADD COLUMN join_policy VARCHAR(50) NOT NULL DEFAULT 'PUBLIC';

-- Create match_requests
CREATE TABLE match_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- JOIN_REQUEST or MATCH_INVITE
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(reservation_id, user_id)
);

CREATE INDEX idx_match_requests_res_id ON match_requests(reservation_id);
CREATE INDEX idx_match_requests_user_id ON match_requests(user_id);

-- Create friend_requests
CREATE TABLE friend_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sender_id, receiver_id),
    CHECK (sender_id != receiver_id)
);

-- Create contact_strikes
CREATE TABLE contact_strikes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    strike_count INT NOT NULL DEFAULT 1,
    last_strike_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(requester_id, target_id),
    CHECK (requester_id != target_id)
);

-- Create user_contacts (Mutual relationship)
CREATE TABLE user_contacts (
    user1_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user1_id, user2_id),
    CHECK (user1_id < user2_id)
);

-- Create user_blocks (Mutual block)
CREATE TABLE user_blocks (
    user1_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user1_id, user2_id),
    CHECK (user1_id < user2_id)
);
