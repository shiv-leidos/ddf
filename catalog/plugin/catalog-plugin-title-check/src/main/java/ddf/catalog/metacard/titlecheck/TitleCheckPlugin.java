/*
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package ddf.catalog.metacard.titlecheck;

import ddf.catalog.data.Metacard;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PreIngestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TitleCheckPlugin implements PreIngestPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(TitleCheckPlugin.class);

  @Override
  public CreateRequest process(CreateRequest input) throws PluginExecutionException {
    if (input.getMetacards().stream().allMatch(this::isHello)) {
      LOGGER.debug("Title check plugin CreateRequest successfully.");
      return input;
    }
    LOGGER.debug("Title check plugin in all metacard are not hello.");
    throw new PluginExecutionException("Title in all metacard are not hello");
  }

  @Override
  public UpdateRequest process(UpdateRequest input) {
    LOGGER.debug("Title check plugin UpdateRequest successfully.");
    return input;
  }

  @Override
  public DeleteRequest process(DeleteRequest input) {
    LOGGER.debug("Title check plugin DeleteRequest successfully.");
    return input;
  }

  private boolean isHello(Metacard metacard) {
    return metacard.getTitle().toLowerCase().equals("hello");
  }
}
