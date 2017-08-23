package com.etsy;

/**
 * An exception indicating that a given channel could not
 * be found.
 */
public class ChannelNotFoundException extends Exception {
  public ChannelNotFoundException() {
    super();
  }

  public ChannelNotFoundException(String message) {
    super(message);
  }

  public ChannelNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
