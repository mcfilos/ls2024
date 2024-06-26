interface IRule
{
    fun isApplicable(node : ProofTreeNode) = isApplicable(node.formula.formulaFactory.logic, node)
    fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean

    fun wouldBranchTheTree() : Boolean

    fun apply(tree : ProofTree, node : ProofTreeNode) = apply(RuleApplyFactory(tree, node), node)
    fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
}

class RuleApplyFactory(val tree : ProofTree, private val node : ProofTreeNode)
{
    fun newNode(formula : IFormula, left : ProofTreeNode? = null, right : ProofTreeNode? = null) : ProofTreeNode
    {
        return node.nodeFactory!!.newNode(formula, left, right)
    }

    fun newFormula(operation : Operation, x : IFormula) : ComplexFormula
    {
        return node.formula.formulaFactory.new(operation, x)
    }

    fun newFormula(x : IFormula, operation : Operation, y : IFormula) : ComplexFormula
    {
        return node.formula.formulaFactory.new(x, operation, y)
    }

    fun newModalRelationDescriptor(fromWorld : PossibleWorld, toWorld : PossibleWorld) : ModalRelationDescriptorFormula
    {
        return node.formula.formulaFactory.newModalRelationDescriptor(fromWorld, toWorld)
    }

    fun newPredicateArgumentInstanceName() : String
    {
        return node.nodeFactory!!.tree.predicateArgumentInstanceNameSequence.next()
    }

    fun getLogic() : ILogic
    {
        return node.formula.formulaFactory.logic
    }
}

class DoubleNegationRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Non
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(left = factory.newNode(node.formula.x.x))
    }
}

class OrRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Or
    }

    override fun wouldBranchTheTree() = true

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(node.formula.x),
            right = factory.newNode(node.formula.y!!),
        )
    }
}

class NotOrRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Or
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                node.formula.formulaFactory.new(Operation.Non, node.formula.x.x),
                left = factory.newNode(node.formula.formulaFactory.new(Operation.Non, node.formula.x.y!!)),
            )
        )
    }
}

class AndRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.And
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x,
                left = factory.newNode(node.formula.y!!),
            )
        )
    }
}

class NotAndRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.And
    }

    override fun wouldBranchTheTree() = true

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(factory.newFormula(Operation.Non, node.formula.x.x)),
            right = factory.newNode(factory.newFormula(Operation.Non, node.formula.x.y!!)),
        )
    }
}

class ImplyRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Imply
    }

    override fun wouldBranchTheTree() = true

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(factory.newFormula(Operation.Non, node.formula.x)),
            right = factory.newNode(node.formula.y!!),
        )
    }
}

class NotImplyRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Imply
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x.x,
                left = factory.newNode(node.formula.formulaFactory.new(Operation.Non, node.formula.x.y!!)),
            )
        )
    }
}

class BiImplyRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.BiImply
    }

    override fun wouldBranchTheTree() = true

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x,
                left = factory.newNode(node.formula.y!!)
            ),
            right = factory.newNode(
                formula = factory.newFormula(Operation.Non, node.formula.x),
                left = factory.newNode(factory.newFormula(Operation.Non, node.formula.y))
            ),
        )
    }
}

class NotBiImplyRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.BiImply
    }

    override fun wouldBranchTheTree() = true

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x.x,
                left = factory.newNode(factory.newFormula(Operation.Non, node.formula.x.y!!))
            ),
            right = factory.newNode(
                formula = factory.newFormula(Operation.Non, node.formula.x.x),
                left = factory.newNode(node.formula.x.y)
            ),
        )
    }
}

class StrictImplicationRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.StrictImply
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            factory.newNode(factory.newFormula(Operation.Necessary(),
                factory.newFormula(node.formula.x, Operation.Imply, node.formula.y!!))))
    }
}

class NotStrictImplicationRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.StrictImply
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            factory.newNode(factory.newFormula(Operation.Non, factory.newFormula(Operation.Necessary(),
                factory.newFormula(node.formula.x.x, Operation.Imply, node.formula.x.y!!)))))
    }
}
