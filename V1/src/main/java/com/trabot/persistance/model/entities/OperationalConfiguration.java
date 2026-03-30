package com.trabot.persistance.model.entities;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationalConfiguration {
    
    @Id
    private String key;
    
    private String value;
    
    public OperationalConfiguration(String key) {
	super();
	this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	OperationalConfiguration other = (OperationalConfiguration) obj;
	return Objects.equals(key, other.key);
    }

    @Override
    public int hashCode() {
	return Objects.hash(key);
    }

    
    

}
