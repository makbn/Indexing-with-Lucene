import scala.util.matching.Regex

val s=".I 1\n.W\ncorrelation between maternal and fetal plasma levels of glucose and free\nfatty acids .                                                           \n  correlation coefficients have been determined between the levels of   \nglucose and ffa in maternal and fetal plasma collected at delivery .    \nsignificant correlations were obtained between the maternal and fetal   \nglucose levels and the maternal and fetal ffa levels . from the size of \nthe correlation coefficients and the slopes of regression lines it      \nappears that the fetal plasma glucose level at delivery is very strongly\ndependent upon the maternal level whereas the fetal ffa level at        \ndelivery is only slightly dependent upon the maternal level .           \n.I 2\n.W\nchanges of the nucleic acid and phospholipid levels of the livers in the\ncourse of fetal and postnatal development .                             \n  we have followed the evolution of dna, rna and pl in the livers of rat\nfoeti removed between the fifteenth and the twenty-first day of         \ngestation and of young rats newly-born or at weaning . we can observe   \nthe following\nfacts.. 1. dna concentration is 1100 ug p on the 15th day, it decreases from\nthe 19th day until it reaches a value of 280 ug 5 days after weaning .  \n  2. rna concentration is 1400 ug p on the 15th day and decreases to 820\nduring the same period .                                                \n  3. pl concentration is low on the 15th day and during foetal life (700\nug) and increases abruptly at birth .                                   \n  4. the ratios rna cyto/dna and pl cyto/dna increase regularly from the\n18th day of foetal life .                                               \n  5. nuclear rna and pl contents are very high throughout the           \ndevelopment .                                                           \n  6. these results enable us to characterize three stages in the        \ndevelopment of the rat liver.. - from the 15th day to the 18th day of   \nfoetal life.. stage of growth through hyperplasia without hypertrophy,  \n  - from the 19th day of foetal life to the 3rd day of post-natal\nlife,. stage of cellular reorganisation,\n  - after the 3rd day of post-natal life.. stage of growth through      \nhyperplasia and hypertrophy ."

val arr=s.split(".I\\s\\d{1,}\n.W")

val start= ".I 345"



var a=1

a.+(1)


val z=arr(1)

