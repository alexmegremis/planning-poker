package com.alexmegremis.planningpoker;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO extends Hideable implements Identifiable, Serializable {
    private String id;
    private String name;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PlayerDTO playerDTO = (PlayerDTO) o;
        return id.equals(playerDTO.id) && name.equals(playerDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    protected String getHideableValue() {
        return name;
    }
}
