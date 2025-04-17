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

import static ddf.catalog.data.types.experimental.Extracted.EXTRACTED_TEXT;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.plugin.PreIngestPlugin;
import ddf.catalog.plugin.StopProcessingException;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TitleCheckPlugin implements PreIngestPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(TitleCheckPlugin.class);

  private final CatalogFramework framework;

  private final FilterBuilder filterBuilder;

  public TitleCheckPlugin(CatalogFramework framework, FilterBuilder filterBuilder) {
    this.framework = framework;
    this.filterBuilder = filterBuilder;
  }

  @Override
  public CreateRequest process(CreateRequest input) throws StopProcessingException {
    try {
      if (input.getMetacards().stream().allMatch(this::isDuplicate)) {
        LOGGER.debug("Title check plugin in all metacard are not hello.");
        throw new StopProcessingException("Title in all metacard are not hello");
      }
    } catch (Exception e) {
      throw new StopProcessingException(e.getMessage());
    }

    LOGGER.debug("Title check plugin CreateRequest successfully.");
    return input;
    // throw new PluginExecutionException("Title in all metacard are not hello");

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
    String extractedText = (String) metacard.getAttribute(EXTRACTED_TEXT).getValue();
    return extractedText.toLowerCase().contains("hello")
        && metacard.getTitle().toLowerCase().equals("hello");
    // return metacard.getTitle().toLowerCase().equals("hello");
  }

  private boolean isDuplicate(Metacard metacard) {
    Attribute attribute = metacard.getAttribute(Core.TITLE);
    if (attribute.getValue() == null) return false;

    String extractedText = (String) metacard.getAttribute(Core.TITLE).getValue();
    Filter filter = filterBuilder.attribute(Core.TITLE).is().text(extractedText);

    QueryRequestImpl queryRequestImpl = new QueryRequestImpl(new QueryImpl(filter));

    try {
      QueryResponse queryResponse = framework.query(queryRequestImpl);
      updateDuplicateMetacardTitle(queryResponse.getResults().get(0).getMetacard());

      return queryResponse.getHits() > 0;
    } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private void updateDuplicateMetacardTitle(Metacard metacard) {
    String id = metacard.getId();
    String title = metacard.getTitle();

    metacard.setAttribute(new AttributeImpl(Core.TITLE, title + "Next"));

    try {
      framework.update(new UpdateRequestImpl(id, metacard));
    } catch (IngestException | SourceUnavailableException e) {
      throw new RuntimeException(e);
    }
  }
}
