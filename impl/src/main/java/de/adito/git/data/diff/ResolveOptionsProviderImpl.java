package de.adito.git.data.diff;

import com.google.inject.Inject;
import de.adito.git.impl.data.diff.ResolveOption;
import de.adito.git.impl.data.diff.ResolveOptionsProvider;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 20.06.2022
 */
public class ResolveOptionsProviderImpl implements ResolveOptionsProvider
{

  private final List<ResolveOption> resolveOptions;

  @Inject
  public ResolveOptionsProviderImpl(Set<ResolveOption> pResolveOptions)
  {
    resolveOptions = pResolveOptions.stream().sorted(Comparator.comparingInt(ResolveOption::getPosition)).collect(Collectors.toList());
  }


  @Override
  public List<ResolveOption> getResolveOptions()
  {
    return resolveOptions;
  }
}
