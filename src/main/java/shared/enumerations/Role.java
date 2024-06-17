package shared.enumerations;

/**
 * The Role enumeration represents different roles in a system.
 */
public enum Role {
  PRACA, SARGENTO, TENENTE, CAPITAO, MAJOR, CORONEL, GENERAL;

  /**
   * Returns the index of the given role.
   *
   * @param role the role to get the index of
   * @return the index of the role, or -1 if the role is not found
   */
  public static int getIndex(Role role) {
    switch (role) {
      case PRACA:
        return 0;
      case SARGENTO:
        return 1;
      case TENENTE:
        return 2;
      case CAPITAO:
        return 3;
      case MAJOR:
        return 4;
      case CORONEL:
        return 5;
      case GENERAL:
        return 6;
      default:
        return -1;
    }
  }

  /**
   * Returns an array of all the role values as strings.
   *
   * @return an array of all the role values as strings
   */
  public static String[] getValues() {
    String[] values = new String[Role.values().length];
    for (int i = 0; i < Role.values().length; i++) {
      values[i] = Role.values()[i].toString();
    }
    return values;
  }
}
