/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab.genetic;

/**
 *
 * @author luca
 */
public class GeneticOptions {
    static public String threshold_name = "Theta"; 
    static public int solution_set_size = 10;
    //can be one of "regularised_logodds", "logodds"
    static public String fitness_type = "regularised_logodds"; 
    static public double size_penalty_coefficient = 1;
    static public double undefined_reference_threshold = 0.1;
    
    static public boolean init__random_number_of_atoms = false;
    static public double init__average_number_of_atoms = 3;
    static public int init__fixed_number_of_atoms = 2;
    static public double init__prob_of_less_than = 0.5;
    static public double init__prob_of_true_atom = 0.01;
    static public double init__and_weight = 1;
    static public double init__or_weight = 1;
    static public double init__not_weight = 1;
    static public double init__imply_weight = 1;
    static public double init__eventually_weight = 1;
    static public double init__globally_weight = 1;
    static public double init__until_weight = 1;
    static public double init__eventuallyglobally_weight = 1;
    static public double init__globallyeventually_weight = 1;
    
    static public double min_time_bound = 0;
    static public double max_time_bound = 100;
    
    
    static public boolean mutate__one_node = true;
    static public double mutate__mutation_probability_per_node = 0.01;
    
    static public double mutate__mutation_probability_one_node = 1;//0.05;
    static public double mutate__insert__weight = 2;
    static public double mutate__delete__weight = 2;
    static public double mutate__replace__weight = 4;
    static public double mutate__change__weight = 0;
    
    static public double mutate__delete__keep_left_node = 0.5;
    
    
    static public double mutate__insert__eventually_weight = 2;
    static public double mutate__insert__globally_weight = 2;
    static public double mutate__insert__negation_weight = 1;
    
    
    static public double mutate__replace__modal_to_modal_weight = 3;
    static public double mutate__replace__modal_to_bool_weight = 1;
    static public double mutate__replace__bool_to_modal_weight = 1;
    static public double mutate__replace__bool_to_bool_weight = 3;
    static public double mutate__replace__keep_left_node = 0.5;
    
    static public double mutate__replace__eventually_weight = 1;
    static public double mutate__replace__globally_weight = 1;
    static public double mutate__replace__until_weight = 1;
    static public double mutate__replace__and_weight = 1;
    static public double mutate__replace__or_weight = 1;
    static public double mutate__replace__imply_weight = 1;
    static public double mutate__replace__not_weight = 1;
    static public double mutate__replace__new_left_node_for_boolean = 0.5;
    static public double mutate__replace__new_left_node_for_until = 0.5;
    static public double mutate__replace__new_left_node_for_until_from_globally = 0.05;
    static public double mutate__replace__new_left_node_for_until_from_eventually = 0.95;
    
    
    static public double mutate__change__prob_lower_bound = 0.5;
    static public double mutate__change__proportion_of_variation = 0.1;

    
    public static void setUndefined_reference_threshold(double undefined_reference_threshold) {
        GeneticOptions.undefined_reference_threshold = undefined_reference_threshold;
    }

    
    public static void setFitness_type(String fitness_type) {
        GeneticOptions.fitness_type = fitness_type;
    }

    public static void setInit__and_weight(double init__and_weight) {
        GeneticOptions.init__and_weight = init__and_weight;
    }

    public static void setInit__average_number_of_atoms(double init__average_number_of_atoms) {
        GeneticOptions.init__average_number_of_atoms = init__average_number_of_atoms;
    }

    public static void setInit__eventually_weight(double init__eventually_weight) {
        GeneticOptions.init__eventually_weight = init__eventually_weight;
    }

    public static void setInit__eventuallyglobally_weight(double init__eventuallyglobally_weight) {
        GeneticOptions.init__eventuallyglobally_weight = init__eventuallyglobally_weight;
    }

    public static void setInit__fixed_number_of_atoms(int init__fixed_number_of_atoms) {
        GeneticOptions.init__fixed_number_of_atoms = init__fixed_number_of_atoms;
    }

    public static void setInit__globally_weight(double init__globally_weight) {
        GeneticOptions.init__globally_weight = init__globally_weight;
    }

    public static void setInit__globallyeventually_weight(double init__globallyeventually_weight) {
        GeneticOptions.init__globallyeventually_weight = init__globallyeventually_weight;
    }

    public static void setInit__imply_weight(double init__imply_weight) {
        GeneticOptions.init__imply_weight = init__imply_weight;
    }

    public static void setInit__not_weight(double init__not_weight) {
        GeneticOptions.init__not_weight = init__not_weight;
    }

    public static void setInit__or_weight(double init__or_weight) {
        GeneticOptions.init__or_weight = init__or_weight;
    }

    public static void setInit__prob_of_less_than(double init__prob_of_less_than) {
        GeneticOptions.init__prob_of_less_than = init__prob_of_less_than;
    }

    public static void setInit__prob_of_true_atom(double init__prob_of_true_atom) {
        GeneticOptions.init__prob_of_true_atom = init__prob_of_true_atom;
    }

    public static void setInit__random_number_of_atoms(boolean init__random_number_of_atoms) {
        GeneticOptions.init__random_number_of_atoms = init__random_number_of_atoms;
    }

    public static void setInit__until_weight(double init__until_weight) {
        GeneticOptions.init__until_weight = init__until_weight;
    }

    public static void setMax_time_bound(double max_time_bound) {
        GeneticOptions.max_time_bound = max_time_bound;
    }

    public static void setMin_time_bound(double min_time_bound) {
        GeneticOptions.min_time_bound = min_time_bound;
    }

    public static void setMutate__change__prob_lower_bound(double mutate__change__prob_lower_bound) {
        GeneticOptions.mutate__change__prob_lower_bound = mutate__change__prob_lower_bound;
    }

    public static void setMutate__change__proportion_of_variation(double mutate__change__proportion_of_variation) {
        GeneticOptions.mutate__change__proportion_of_variation = mutate__change__proportion_of_variation;
    }

    public static void setMutate__change__weight(double mutate__change__weight) {
        GeneticOptions.mutate__change__weight = mutate__change__weight;
    }

    public static void setMutate__delete__keep_left_node(double mutate__delete__keep_left_node) {
        GeneticOptions.mutate__delete__keep_left_node = mutate__delete__keep_left_node;
    }

    public static void setMutate__delete__weight(double mutate__delete__weight) {
        GeneticOptions.mutate__delete__weight = mutate__delete__weight;
    }

    public static void setMutate__insert__eventually_weight(double mutate__insert__eventually_weight) {
        GeneticOptions.mutate__insert__eventually_weight = mutate__insert__eventually_weight;
    }

    public static void setMutate__insert__globally_weight(double mutate__insert__globally_weight) {
        GeneticOptions.mutate__insert__globally_weight = mutate__insert__globally_weight;
    }

    public static void setMutate__insert__negation_weight(double mutate__insert__negation_weight) {
        GeneticOptions.mutate__insert__negation_weight = mutate__insert__negation_weight;
    }

    public static void setMutate__insert__weight(double mutate__insert__weight) {
        GeneticOptions.mutate__insert__weight = mutate__insert__weight;
    }

    public static void setMutate__mutation_probability_one_node(double mutate__mutation_probability_one_node) {
        GeneticOptions.mutate__mutation_probability_one_node = mutate__mutation_probability_one_node;
    }

    public static void setMutate__mutation_probability_per_node(double mutate__mutation_probability_per_node) {
        GeneticOptions.mutate__mutation_probability_per_node = mutate__mutation_probability_per_node;
    }

    public static void setMutate__one_node(boolean mutate__one_node) {
        GeneticOptions.mutate__one_node = mutate__one_node;
    }

    public static void setMutate__replace__and_weight(double mutate__replace__and_weight) {
        GeneticOptions.mutate__replace__and_weight = mutate__replace__and_weight;
    }

    public static void setMutate__replace__bool_to_bool_weight(double mutate__replace__bool_to_bool_weight) {
        GeneticOptions.mutate__replace__bool_to_bool_weight = mutate__replace__bool_to_bool_weight;
    }

    public static void setMutate__replace__bool_to_modal_weight(double mutate__replace__bool_to_modal_weight) {
        GeneticOptions.mutate__replace__bool_to_modal_weight = mutate__replace__bool_to_modal_weight;
    }

    public static void setMutate__replace__eventually_weight(double mutate__replace__eventually_weight) {
        GeneticOptions.mutate__replace__eventually_weight = mutate__replace__eventually_weight;
    }

    public static void setMutate__replace__globally_weight(double mutate__replace__globally_weight) {
        GeneticOptions.mutate__replace__globally_weight = mutate__replace__globally_weight;
    }

    public static void setMutate__replace__imply_weight(double mutate__replace__imply_weight) {
        GeneticOptions.mutate__replace__imply_weight = mutate__replace__imply_weight;
    }

    public static void setMutate__replace__keep_left_node(double mutate__replace__keep_left_node) {
        GeneticOptions.mutate__replace__keep_left_node = mutate__replace__keep_left_node;
    }

    public static void setMutate__replace__modal_to_bool_weight(double mutate__replace__modal_to_bool_weight) {
        GeneticOptions.mutate__replace__modal_to_bool_weight = mutate__replace__modal_to_bool_weight;
    }

    public static void setMutate__replace__modal_to_modal_weight(double mutate__replace__modal_to_modal_weight) {
        GeneticOptions.mutate__replace__modal_to_modal_weight = mutate__replace__modal_to_modal_weight;
    }

    public static void setMutate__replace__new_left_node_for_boolean(double mutate__replace__new_left_node_for_boolean) {
        GeneticOptions.mutate__replace__new_left_node_for_boolean = mutate__replace__new_left_node_for_boolean;
    }

    public static void setMutate__replace__new_left_node_for_until(double mutate__replace__new_left_node_for_until) {
        GeneticOptions.mutate__replace__new_left_node_for_until = mutate__replace__new_left_node_for_until;
    }

    public static void setMutate__replace__new_left_node_for_until_from_eventually(double mutate__replace__new_left_node_for_until_from_eventually) {
        GeneticOptions.mutate__replace__new_left_node_for_until_from_eventually = mutate__replace__new_left_node_for_until_from_eventually;
    }

    public static void setMutate__replace__new_left_node_for_until_from_globally(double mutate__replace__new_left_node_for_until_from_globally) {
        GeneticOptions.mutate__replace__new_left_node_for_until_from_globally = mutate__replace__new_left_node_for_until_from_globally;
    }

    public static void setMutate__replace__not_weight(double mutate__replace__not_weight) {
        GeneticOptions.mutate__replace__not_weight = mutate__replace__not_weight;
    }

    public static void setMutate__replace__or_weight(double mutate__replace__or_weight) {
        GeneticOptions.mutate__replace__or_weight = mutate__replace__or_weight;
    }

    public static void setMutate__replace__until_weight(double mutate__replace__until_weight) {
        GeneticOptions.mutate__replace__until_weight = mutate__replace__until_weight;
    }

    public static void setMutate__replace__weight(double mutate__replace__weight) {
        GeneticOptions.mutate__replace__weight = mutate__replace__weight;
    }

    public static void setSize_penalty_coefficient(double size_penalty_coefficient) {
        GeneticOptions.size_penalty_coefficient = size_penalty_coefficient;
    }

    public static void setSolution_set_size(int solution_set_size) {
        GeneticOptions.solution_set_size = solution_set_size;
    }

    public static void setThreshold_name(String threshold_name) {
        GeneticOptions.threshold_name = threshold_name;
    }
 
    
    
    
}
