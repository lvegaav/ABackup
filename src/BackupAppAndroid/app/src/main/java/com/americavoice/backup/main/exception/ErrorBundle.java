
package com.americavoice.backup.main.exception;

/**
 * Interface to represent a wrapper around an {@link Exception} to manage errors.
 */
public interface ErrorBundle {
  Exception getException();

  String getErrorMessage();
}
