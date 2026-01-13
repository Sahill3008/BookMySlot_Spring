package com.secure.appointment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 'user' is a reserved keyword in Postgres, so we use 'users'
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password; // BCrypt Hash

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	public User() {
	}

	public User(Long id, String email, String password, String name, Role role, boolean isActive) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.name = name;
		this.role = role;
		this.isActive = isActive;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String email;
		private String password;
		private String name;
		private Role role;
		private boolean isActive = true;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder role(Role role) {
			this.role = role;
			return this;
		}

		public Builder isActive(boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		public User build() {
			return new User(id, email, password, name, role, isActive);
		}
	}
}
