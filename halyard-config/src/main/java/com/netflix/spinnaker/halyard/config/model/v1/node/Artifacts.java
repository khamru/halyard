/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.netflix.spinnaker.halyard.config.model.v1.node;

import com.netflix.spinnaker.halyard.config.model.v1.artifacts.gcs.GcsArtifactProvider;
import com.netflix.spinnaker.halyard.config.model.v1.artifacts.github.GitHubArtifactProvider;
import com.netflix.spinnaker.halyard.config.model.v1.artifacts.http.HttpArtifactProvider;
import com.netflix.spinnaker.halyard.config.model.v1.artifacts.s3.S3ArtifactProvider;
import com.netflix.spinnaker.halyard.config.problem.v1.ConfigProblemSetBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public class Artifacts extends Node {
  GcsArtifactProvider gcs = new GcsArtifactProvider();
  GitHubArtifactProvider github = new GitHubArtifactProvider();
  HttpArtifactProvider http = new HttpArtifactProvider();
  S3ArtifactProvider s3 = new S3ArtifactProvider();

  @Override
  public String getNodeName() {
    return "provider";
  }

  @Override
  public NodeIterator getChildren() {
    return NodeIteratorFactory.makeReflectiveIterator(this);
  }

  @Override
  public void accept(ConfigProblemSetBuilder psBuilder, Validator v) {
    v.validate(psBuilder, this);
  }

  public static Class<? extends ArtifactProvider> translateArtifactProviderType(String providerName) {
    Optional<? extends Class<?>> res = Arrays.stream(Artifacts.class.getDeclaredFields())
        .filter(f -> f.getName().equals(providerName))
        .map(Field::getType)
        .findFirst();

    if (res.isPresent()) {
      return (Class<? extends ArtifactProvider>)res.get();
    } else {
      throw new IllegalArgumentException("No artifact provider with name \"" + providerName + "\" handled by halyard");
    }
  }

  public static Class<? extends ArtifactAccount> translateArtifactAccountType(String providerName) {
    Class<? extends ArtifactProvider> providerClass = translateArtifactProviderType(providerName);

    String accountClassName = providerClass.getName().replaceAll("Provider", "Account");
    try {
      return (Class<? extends ArtifactAccount>) Class.forName(accountClassName);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("No artifact account for class \"" + accountClassName + "\" found", e);
    }
  }
}
