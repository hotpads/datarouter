/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.instrumentation.metric.node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.instrumentation.metric.MetricRecorder;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto.Nested.MetricTemplateNodeDto;

public class MetricNode extends MetricRecorder{

	protected MetricNode parent;
	protected String patternName;
	protected String value;

	protected MetricNode literal(String literal){
		return literal(MetricNode::new, literal);
	}

	protected <P extends MetricNode> P literal(Supplier<P> childSupplier, String literal){
		P child = childSupplier.get();
		child.parent = this;
		child.value = literal;
		return child;
	}

	protected <P extends MetricNodeVariable<P>> P variable(Supplier<P> childSupplier, String varValue){
		P child = childSupplier.get();
		child.parent = this;
		child.patternName = child.name;
		child.value = varValue;
		return child;
	}

	/**
	 * ***For building string metric name only***<br/><br/>
	 * Overrides supplied node with a custom value then returns the continued name chain
	 * @param childSupplier Where the name chain should continue from
	 * @param customValue Custom override string for next segment
	 * @return supplied child node
	 */
	public <P extends MetricNode> P override(Supplier<P> childSupplier, String customValue){
		P child = childSupplier.get();
		child.parent = this;
		child.value = customValue;
		return child;
	}

	@Override
	public String toMetricName(){
		return streamOrderedNodes()
				.map(el -> el.value)
				.collect(Collectors.joining(io.datarouter.instrumentation.metric.token.MetricToken.DELIMITER));
	}

	@Override
	protected MetricTemplateDto makePatternDto(String description){
		List<MetricTemplateNodeDto> nodeDtos = streamOrderedNodes()
				.map(node -> {
					if(node instanceof MetricNodeVariable<?> e){
						if(e.value == null){
							// this is a pattern node, wait for variable value node
							return Optional.<MetricTemplateNodeDto>empty();
						}
						return Optional.of(new MetricTemplateNodeDto(true, e.patternName, e.description));
					}
					return Optional.of(new MetricTemplateNodeDto(false, node.value, null));
				})
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();

		return new MetricTemplateDto(nodeDtos, description);
	}

	private Stream<MetricNode> streamOrderedNodes(){
		Deque<MetricNode> nodes = new ArrayDeque<>();
		MetricNode curr = this;
		while(curr != null){
			if(curr.value != null){
				nodes.push(curr);
			}
			curr = curr.parent;
		}
		return nodes.stream();
	}

	public abstract static class MetricNodeVariable<T extends MetricNodeVariable<T>> extends MetricNode{

		protected final String name;
		protected final String description;
		protected final Supplier<T> self;

		public MetricNodeVariable(MetricNodeVariableDefinition definition, Supplier<T> self){
			this(definition.name(), definition.description(), self);
		}

		public MetricNodeVariable(String name, String description, Supplier<T> self){
			this.name = name;
			this.description = description;
			this.self = self;
		}

		protected T as(String variableValue){
			T copy = self.get();
			copy.parent = this;
			copy.patternName = patternName;
			copy.value = variableValue;
			return copy;
		}

	}

	public record MetricNodeVariableDefinition(
			String name,
			String description){
	}

}
